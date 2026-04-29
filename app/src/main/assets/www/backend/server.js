/**
 * ShopIndia Backend — Node.js + Express + JSON File DB
 * Swap DB.read/write with Mongoose when upgrading to MongoDB
 */
const express = require('express');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors({ origin: process.env.ALLOWED_ORIGIN || '*' }));
app.use(express.json({ limit: '10mb' }));
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
app.use('/frontend', express.static(path.join(__dirname, '..', 'frontend')));
app.use('/admin', express.static(path.join(__dirname, '..', 'admin')));

['./data','./uploads','./uploads/payments','./uploads/products'].forEach(d => {
  if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
});

const DB = {
  read: (col) => { try { const f=path.join(__dirname,'data',col+'.json'); if(!fs.existsSync(f)) return []; return JSON.parse(fs.readFileSync(f,'utf8')); } catch { return []; } },
  write: (col, data) => { fs.writeFileSync(path.join(__dirname,'data',col+'.json'), JSON.stringify(data,null,2)); },
  insert: (col, item) => { const d=DB.read(col); const n={id:crypto.randomUUID(),createdAt:new Date().toISOString(),...item}; d.push(n); DB.write(col,d); return n; },
  update: (col, id, up) => { const d=DB.read(col); const i=d.findIndex(x=>x.id===id); if(i===-1) return null; d[i]={...d[i],...up,updatedAt:new Date().toISOString()}; DB.write(col,d); return d[i]; },
  findById: (col, id) => DB.read(col).find(x=>x.id===id)||null,
  delete: (col, id) => { let d=DB.read(col); const n=d.length; d=d.filter(x=>x.id!==id); DB.write(col,d); return d.length<n; },
};

const payUpload = multer({ storage: multer.diskStorage({ destination: (_,__,cb) => cb(null,'./uploads/payments'), filename: (_,f,cb) => cb(null,'pay_'+Date.now()+path.extname(f.originalname)) }), limits: {fileSize:5*1024*1024} });
const prodUpload = multer({ storage: multer.diskStorage({ destination: (_,__,cb) => cb(null,'./uploads/products'), filename: (_,f,cb) => cb(null,'prod_'+Date.now()+path.extname(f.originalname)) }), limits: {fileSize:5*1024*1024} });

const adminAuth = (req, res, next) => {
  const token = req.headers['x-admin-token'];
  if (token !== (process.env.ADMIN_TOKEN || 'shopindia-admin-secret-token')) return res.status(401).json({ error: 'Unauthorized' });
  next();
};

// Seed
if (DB.read('products').length === 0) {
  const seeds = [
    {name:'Floral Print Cotton Kurti',category:'Kurti Saree and Lehanga',price:349,orig:799,img:'https://images.unsplash.com/photo-1585744481400-9b5e6cedc30c?w=400&q=80',rating:4.5,reviews:1243,stock:'in-stock',supplier:'Meesho Supplier A',supplierSku:'MEE-KS-001',handlingDays:2},
    {name:'Wireless Bluetooth Earphones',category:'Electronics',price:599,orig:1999,img:'https://images.unsplash.com/photo-1572536147248-ac59a8abfa4b?w=400&q=80',rating:4.3,reviews:5621,stock:'in-stock',supplier:'Electronics Bazaar',supplierSku:'EB-TWS-003',handlingDays:1},
    {name:'Korean Face Cream SPF50',category:'Beauty and Health',price:249,orig:599,img:'https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400&q=80',rating:4.6,reviews:3214,stock:'limited',supplier:'Beauty Korea Import',supplierSku:'BKI-FC-004',handlingDays:2},
  ];
  seeds.forEach(p => DB.insert('products', p));
}

app.get('/api/health', (req, res) => res.json({ status:'ok', store:'ShopIndia', orders:DB.read('orders').length, products:DB.read('products').length }));

// Products
app.get('/api/products', (req, res) => {
  let products = DB.read('products');
  const { category, search, sort, limit=20, offset=0 } = req.query;
  if (category && category !== 'all') products = products.filter(p => p.category === category);
  if (search) { const s=search.toLowerCase(); products = products.filter(p => p.name.toLowerCase().includes(s)); }
  if (sort === 'price-low') products.sort((a,b) => a.price-b.price);
  if (sort === 'price-high') products.sort((a,b) => b.price-a.price);
  if (sort === 'rating') products.sort((a,b) => b.rating-a.rating);
  res.json({ products: products.slice(+offset, +offset + +limit), total: products.length });
});
app.get('/api/products/:id', (req, res) => { const p=DB.findById('products',req.params.id); p?res.json(p):res.status(404).json({error:'Not found'}); });
app.post('/api/products', adminAuth, (req, res) => { const p=DB.insert('products',req.body); res.status(201).json(p); });
app.put('/api/products/:id', adminAuth, (req, res) => { const p=DB.update('products',req.params.id,req.body); p?res.json(p):res.status(404).json({error:'Not found'}); });
app.delete('/api/products/:id', adminAuth, (req, res) => { DB.delete('products',req.params.id)?res.json({success:true}):res.status(404).json({error:'Not found'}); });
app.post('/api/products/upload-image', adminAuth, prodUpload.single('image'), (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file' });
  res.json({ url: `/uploads/products/${req.file.filename}` });
});

// Orders
app.get('/api/orders', adminAuth, (req, res) => {
  let orders = DB.read('orders');
  const { status, search } = req.query;
  if (status && status !== 'all') orders = orders.filter(o => o.status?.toLowerCase().includes(status));
  if (search) { const s=search.toLowerCase(); orders=orders.filter(o=>o.id?.toLowerCase().includes(s)||o.name?.toLowerCase().includes(s)); }
  res.json({ orders: [...orders].reverse(), total: orders.length });
});
app.get('/api/orders/:id', (req, res) => {
  const order = DB.findById('orders', req.params.id);
  if (!order) return res.status(404).json({ error: 'Not found' });
  const adminToken = req.headers['x-admin-token'];
  if (adminToken !== (process.env.ADMIN_TOKEN || 'shopindia-admin-secret-token')) {
    if (!req.query.phone || order.phone !== req.query.phone) return res.status(403).json({ error: 'Access denied' });
  }
  res.json(order);
});
app.post('/api/orders', (req, res) => {
  const { name, phone, address, city, state, pin, items, total, paymentMethod } = req.body;
  if (!name||!phone||!address||!items?.length||!total) return res.status(400).json({ error: 'Missing required fields' });
  const orderId = 'SI-' + Date.now().toString().slice(-6);
  const order = DB.insert('orders', { ...req.body, id: orderId, status: paymentMethod==='cod'?'Confirmed (COD)':'Payment Verification Pending' });
  // EMAIL_TRIGGER: sendOrderConfirmationEmail(order)
  // WHATSAPP_TRIGGER: sendWhatsApp(phone, orderId)
  res.status(201).json({ success: true, orderId, order });
});
app.put('/api/orders/:id/status', adminAuth, (req, res) => {
  const { status, trackingNumber, courier } = req.body;
  if (!status) return res.status(400).json({ error: 'Status required' });
  const updated = DB.update('orders', req.params.id, { status, trackingNumber, courier });
  if (!updated) return res.status(404).json({ error: 'Not found' });
  // EMAIL_TRIGGER: sendStatusEmail(updated.email, req.params.id, status)
  res.json(updated);
});
app.post('/api/orders/:id/screenshot', payUpload.single('screenshot'), (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file' });
  DB.update('orders', req.params.id, { paymentScreenshot: `/uploads/payments/${req.file.filename}`, paymentUploadedAt: new Date().toISOString() });
  res.json({ success: true, screenshotUrl: `/uploads/payments/${req.file.filename}` });
});

// Coupon validation
app.post('/api/coupon/validate', (req, res) => {
  const coupons = { 'FLAT10':{type:'percent',value:10,minOrder:0}, 'SHIP49':{type:'flat',value:49,minOrder:199}, 'SAVE50':{type:'flat',value:50,minOrder:499}, 'NEW100':{type:'flat',value:100,minOrder:799} };
  const { code, subtotal } = req.body;
  const coupon = coupons[code?.toUpperCase()];
  if (!coupon) return res.status(404).json({ valid: false, error: 'Invalid coupon' });
  if (subtotal < coupon.minOrder) return res.status(400).json({ valid: false, error: `Min order ₹${coupon.minOrder} required` });
  const discount = coupon.type==='percent' ? Math.floor(subtotal*coupon.value/100) : coupon.value;
  res.json({ valid: true, discount, coupon: { code: code.toUpperCase(), ...coupon } });
});

// Pincode check
app.get('/api/pincode/:pin', (req, res) => {
  const { pin } = req.params;
  const serviceable = ['0','1','2','3','4','5','6','7'].includes(pin[0]);
  res.json({ pin, serviceable, estimatedDays: serviceable ? Math.floor(Math.random()*3)+4 : null });
});

// Reviews
app.get('/api/reviews/:productId', (req, res) => res.json(DB.read('reviews').filter(r => r.productId === req.params.productId)));
app.post('/api/reviews', (req, res) => { const r=DB.insert('reviews',req.body); res.status(201).json(r); });

// Subscribe
app.post('/api/subscribe', (req, res) => {
  const { phone, email } = req.body;
  if (!phone && !email) return res.status(400).json({ error: 'Phone or email required' });
  DB.insert('subscribers', { phone, email });
  // EMAIL_TRIGGER: addToEmailList(email)
  res.json({ success: true });
});

// Contact
app.post('/api/contact', (req, res) => {
  const { name, phone, message } = req.body;
  if (!name||!phone||!message) return res.status(400).json({ error: 'All fields required' });
  DB.insert('contacts', { name, phone, message });
  // EMAIL_TRIGGER: notifyAdmin(name, phone, message)
  res.json({ success: true });
});

// Cart sync (abandoned cart tracking)
app.post('/api/cart/save', (req, res) => {
  const { userId, cart, timestamp } = req.body;
  const carts = DB.read('carts');
  const idx = carts.findIndex(c => c.userId === userId);
  if (idx !== -1) { carts[idx] = { ...carts[idx], cart, timestamp, ordered: false }; }
  else { carts.push({ userId, cart, timestamp: timestamp||Date.now(), ordered: false, id: crypto.randomUUID() }); }
  DB.write('carts', carts);
  // ABANDONED_CART_TRIGGER: schedule email if not ordered in 30min
  res.json({ success: true });
});

// Admin auth
app.post('/api/admin/login', (req, res) => {
  const { username, password } = req.body;
  const validUser = process.env.ADMIN_USER || 'admin';
  const validPass = process.env.ADMIN_PASS || 'shopindia123';
  if (username === validUser && password === validPass) {
    res.json({ success: true, token: process.env.ADMIN_TOKEN || 'shopindia-admin-secret-token' });
  } else {
    res.status(401).json({ error: 'Invalid credentials' });
  }
});

// Analytics
app.get('/api/analytics', adminAuth, (req, res) => {
  const orders = DB.read('orders');
  const confirmed = orders.filter(o => !o.status?.includes('Cancel'));
  const totalRevenue = confirmed.reduce((s,o) => s+(o.total||0), 0);
  const byStatus = {}, byPayment = {};
  orders.forEach(o => { byStatus[o.status]=(byStatus[o.status]||0)+1; byPayment[o.paymentMethod||'unknown']=(byPayment[o.paymentMethod||'unknown']||0)+1; });
  res.json({ totalRevenue, totalOrders: orders.length, avgOrderValue: orders.length?Math.round(totalRevenue/orders.length):0, byStatus, byPayment });
});

// Export CSV
app.get('/api/export/orders', adminAuth, (req, res) => {
  const orders = DB.read('orders');
  const csv = ['Order ID,Customer,Phone,Total,Payment,Status,Date',
    ...orders.map(o => `${o.id},"${o.name}",${o.phone},${o.total},${o.paymentMethod},${o.status},${new Date(o.createdAt).toLocaleDateString('en-IN')}`)
  ].join('\n');
  res.setHeader('Content-Type','text/csv');
  res.setHeader('Content-Disposition','attachment; filename=shopindia-orders.csv');
  res.send(csv);
});
app.get('/api/export/dropship', adminAuth, (req, res) => {
  const orders = DB.read('orders').filter(o => o.status==='Confirmed'||o.status==='Processing');
  const rows = [];
  orders.forEach(o => (o.supplierInfo||[]).forEach(s => rows.push(`${o.id},"${s.productName}",${s.qty},"${s.supplier}","${s.supplierSku}","${o.name}",${o.phone}`)));
  res.setHeader('Content-Type','text/csv');
  res.setHeader('Content-Disposition','attachment; filename=dropship-orders.csv');
  res.send(['Order ID,Product,Qty,Supplier,SKU,Customer,Phone',...rows].join('\n'));
});

app.use((err, req, res, next) => { res.status(500).json({ error: err.message }); });
app.use('*', (req, res) => res.status(404).json({ error: 'Not found' }));

app.listen(PORT, () => console.log(`\n🛍️  ShopIndia Server v2.0\n📦 Store: http://localhost:${PORT}/frontend/index.html\n⚙️  Admin: http://localhost:${PORT}/admin/index.html\n💡 API:   http://localhost:${PORT}/api/health\n`));
module.exports = app;

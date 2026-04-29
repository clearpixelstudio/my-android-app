# 🛍️ ShopIndia™ — Complete Dropshipping E-Commerce Platform v2.0

A **production-ready**, mobile-first dropshipping store for India.  
UPI + COD payments · GST invoice · Admin dashboard · Dropship hub

---

## 📁 Project Structure

```
shopindia/
├── frontend/
│   └── index.html          ← Complete PWA store (all pages in one file)
├── admin/
│   └── index.html          ← Admin dashboard (dark theme)
├── backend/
│   ├── server.js           ← Express API server
│   ├── package.json
│   ├── .env.example        ← Environment variables template
│   ├── data/               ← JSON file DB (auto-created)
│   └── uploads/            ← Payment screenshots & product images
└── README.md
```

---

## 🚀 Quick Start

### Option A: Static Only (No Backend — Netlify/GitHub Pages)
```bash
# Just open frontend/index.html in browser
# Everything runs on localStorage
# OR drag & drop frontend/ folder to netlify.com
```

### Option B: Full Stack (Recommended for production)
```bash
cd backend
npm install
cp .env.example .env
# Edit .env with your real values
node server.js
```

Then open:
- 🌐 **Store**: http://localhost:3001/frontend/index.html
- ⚙️ **Admin**: http://localhost:3001/admin/index.html
- 📡 **API**: http://localhost:3001/api/health

---

## 🔐 Admin Access

| Field    | Default Value         |
|----------|-----------------------|
| URL      | `/admin/index.html`   |
| Username | `admin`               |
| Password | `shopindia123`        |

⚠️ **Change these immediately in Settings after first login!**

---

## 🌐 Deployment

### Frontend → Netlify (Free)
```bash
# Option 1: Drag & drop the /frontend folder to app.netlify.com
# Option 2: CLI
npm install -g netlify-cli
netlify deploy --dir=frontend --prod
```

### Admin → Netlify (separate deploy)
```bash
netlify deploy --dir=admin --prod
```

### Backend → Render.com (Free tier)
1. Push this entire project to GitHub
2. Go to render.com → New Web Service
3. Connect your GitHub repo
4. Settings:
   - **Root directory**: `backend`
   - **Build command**: `npm install`
   - **Start command**: `node server.js`
5. Add Environment Variables from `.env.example`
6. Deploy!

### Backend → Railway (alternative)
```bash
npm install -g railway
railway login
railway init
railway up
```

---

## 🎨 Customization Guide

### 1. Change Brand Name & Colors
Open `frontend/index.html`, find the CONFIG section at the top of the `<script>` tag:
```javascript
const CONFIG = {
  storeName: 'ShopIndia',      // ← Change brand name
  upiId: 'tanvir786@ptaxis',   // ← Your UPI ID
  whatsapp: '917866048085',    // ← Your WhatsApp (91 + 10 digits)
  freeShippingAbove: 499,      // ← Free shipping threshold
  standardShipping: 49,        // ← Standard delivery charge
  expressShipping: 99,         // ← Express delivery charge
  gstRate: 0.18,               // ← GST rate (18%)
};
```

To change colors, edit the CSS variables at the top of `<style>`:
```css
:root {
  --primary: #f43f5e;      /* Main brand color — change this */
  --primary-dark: #e11d48; /* Darker shade */
  --secondary: #6366f1;    /* Accent color */
  --accent: #f59e0b;       /* Warning/highlight color */
  --success: #10b981;      /* Success green */
}
```

### 2. Change Logo / Store Name
Search for `ShopIndia` in both HTML files and replace with your brand name.

### 3. Add / Edit Products
**Via Admin Panel** (recommended):
1. Open `/admin/index.html`
2. Login → Products → Add Product
3. Fill all fields including Supplier info

**Via Code** (in frontend/index.html):
```javascript
const PRODUCTS = [
  {
    id: 17,
    name: 'Your Product Name',
    category: 'Electronics',          // Must match a CATEGORY name
    price: 499,                        // Selling price (₹)
    orig: 999,                         // MRP (₹)
    img: 'https://your-image-url.jpg', // Product image
    rating: 4.5,
    reviews: 500,
    badge: '50% OFF',
    desc: 'Product description here.',
    highlights: ['Feature 1', 'Feature 2', 'Feature 3'],
    specs: [['Material','Cotton'], ['Size','Free Size']],
    // Supplier info (for dropshipping):
    supplier: 'Your Supplier Name',
    supplierSku: 'SUP-PROD-001',
    handlingDays: 2,
  },
  // ... add more products
];
```

### 4. Add Coupon Codes
In `frontend/index.html` CONFIG section:
```javascript
coupons: {
  'FLAT10': { type:'percent', value:10, minOrder:0, desc:'10% off' },
  'SAVE100': { type:'flat', value:100, minOrder:599, desc:'₹100 off on ₹599+' },
  // Add more coupons here
},
```

### 5. Add Serviceable Pincodes
```javascript
serviceablePincodes: [
  '110001', '400001', '500001', // Add your delivery pincodes
  // Get full list from your courier partner's API
],
```

### 6. Add Razorpay Payment Gateway
1. Sign up at razorpay.com → get Test API keys
2. In `frontend/index.html`, find the comment `// RAZORPAY INTEGRATION:`
3. Uncomment and add your key:
```javascript
const CONFIG = {
  razorpayKeyId: 'rzp_test_xxxxxxxxxx', // Add this line
};
```
4. In `placeOrder()` function, uncomment the Razorpay code block

### 7. Adjust Shipping Rules
```javascript
const CONFIG = {
  freeShippingAbove: 499,    // Orders above ₹499 get free shipping
  standardShipping: 49,      // Standard delivery charge
  expressShipping: 99,       // Express delivery charge
};
```

### 8. Change Company/GST Details (for invoices)
In `admin/index.html`, go to Settings tab:
- Store name, GSTIN, seller address
- These appear on GST invoices

---

## 📦 Order Workflow (Dropshipping)

```
Customer places order
        ↓
Payment verified (UPI screenshot or COD)
        ↓
Admin gets notification (WhatsApp / Email)
        ↓
Admin opens Dropship Hub → sees supplier info
        ↓
Admin orders on Meesho / Amazon / local supplier
        ↓
Admin enters tracking number → marks "Shipped"
        ↓
Customer gets WhatsApp notification
        ↓
Order delivered → marked "Delivered"
```

---

## 💳 Payment Methods

| Method | Status | Notes |
|--------|--------|-------|
| UPI (screenshot) | ✅ Ready | Customer uploads payment proof |
| Cash on Delivery | ✅ Ready | No advance payment |
| Razorpay (Card/UPI/Net banking) | 🔧 Wire in | Add key in CONFIG |

---

## 📧 Email Integration (Plug-in Ready)

All email trigger points are commented in `server.js`:
```
// EMAIL_TRIGGER: sendOrderConfirmationEmail(order)
// EMAIL_TRIGGER: sendStatusEmail(updated.email, orderId, status)
// ABANDONED_CART_TRIGGER: schedule email if not ordered in 30min
```

To integrate email, install nodemailer:
```bash
npm install nodemailer
```

Then add SMTP config to `.env` and implement the functions in `server.js`.

---

## 🏦 API Reference

```
GET    /api/health                    Health check
GET    /api/products                  List products (filter, sort, paginate)
GET    /api/products/:id              Single product
POST   /api/products (admin)          Add product
PUT    /api/products/:id (admin)      Update product
DELETE /api/products/:id (admin)      Delete product
POST   /api/orders                    Place order
GET    /api/orders (admin)            List all orders
GET    /api/orders/:id                Order detail
PUT    /api/orders/:id/status (admin) Update order status
POST   /api/orders/:id/screenshot     Upload payment screenshot
POST   /api/coupon/validate           Validate coupon code
GET    /api/pincode/:pin              Check delivery availability
POST   /api/reviews                   Submit review
GET    /api/reviews/:productId        Product reviews
POST   /api/subscribe                 Newsletter signup
POST   /api/contact                   Contact form
GET    /api/analytics (admin)         Analytics data
GET    /api/export/orders (admin)     Export orders CSV
GET    /api/export/dropship (admin)   Export supplier orders CSV
POST   /api/admin/login               Admin login
```

Admin API requires header: `x-admin-token: your-token`

---

## 🔧 Upgrade to MongoDB (Production)

When you're ready to scale:

```bash
npm install mongoose
```

Then in `server.js`, replace `DB.read()` / `DB.write()` / `DB.insert()` calls with Mongoose model operations:

```javascript
// Instead of: DB.read('orders')
// Use: await Order.find({})

// Instead of: DB.insert('orders', data)
// Use: await new Order(data).save()
```

---

## 📱 PWA (App-like experience)

The store works like a mobile app:
- Sticky header + bottom navigation
- Swipe-friendly product galleries
- Add to home screen support
- Offline-ready structure

---

## 🛡️ Security Notes

- Never commit `.env` to Git (add to `.gitignore`)
- Change admin password immediately after first login
- Razorpay secret goes in backend `.env` ONLY — never in frontend
- For production, set `ALLOWED_ORIGIN` to your exact domain in `.env`

---

## 📞 Support

- WhatsApp: +91 78660 48085
- UPI: tanvir786@ptaxis

---

*Built with ❤️ for Indian dropshipping & reseller businesses*
*ShopIndia™ v2.0 | Mobile-first | India-ready | Deploy-ready*

# G-Store MCP Server for Google Gemini Spark

This server implements the **Model Context Protocol (MCP)** to allow **Google Gemini** to directly manage your **G-Store (Rice & Grocery Mart)** AWS AppSync & DynamoDB cloud backend with AI image generation and Cloudinary CDN optimization.

---

## 🛠️ Complete Suite of 23 MCP Tools

### 📦 1. Catalog & Multi-Variant Management (11 Tools)
1. **`list_products`**: Lists all products, categories, pack sizes, selling prices, MRPs, and stock indicators.
2. **`get_product_by_id`**: Fetches full details for a product by ID.
3. **`create_product`**: Creates a new product, automatically uploading image to Cloudinary CDN (`ricemart_products`).
4. **`update_product_details`**: Updates product name, Telugu name, brand, category, description, image, or listing visibility (`isListed`).
5. **`generate_and_upload_product_image`**: Generates a photo-realistic, studio e-commerce grocery product image using AI or processes a reference photo taken by your uncle on his phone, formatting it to a uniform 600x600 square on a clean white background, uploading it to Cloudinary, and attaching it to the product in AWS.
6. **`add_product_variant`**: Adds a new weight/volume size variant (e.g. 5kg, 10kg, 26kg, 500ml, 1L) to an existing product with its own price, MRP, and stock.
7. **`delete_product_variant`**: Removes a specific size variant from a product listing.
8. **`update_product_price_or_stock`**: Updates the price or stock count for a specific product variant.
9. **`bulk_update_stock_or_prices`**: Batch updates stock counts or prices across multiple products/variants in a single call (e.g., for wholesale truck arrivals).
10. **`delete_product`**: Removes a product completely from the catalog.
11. **`get_low_stock_alerts`**: Lists all products running low on stock (< threshold units/bags).

### 🛍️ 2. Orders & Omnichannel Fulfillment (5 Tools)
12. **`create_order`**: Records walk-in, phone, or WhatsApp orders directly into AppSync/DynamoDB and deducts stock.
13. **`generate_order_invoice`**: Generates a clean text/WhatsApp bill with customer details, items, prices, and address for delivery boys.
14. **`list_orders`**: Lists customer orders with status filtering (`PENDING`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`).
15. **`search_customer_orders`**: Looks up order history and lifetime spend by phone number or name.
16. **`update_order_status`**: Advances fulfillment status (`PENDING` -> `PREPARING` -> `OUT_FOR_DELIVERY` -> `DELIVERED`).

### 📈 3. Sales Analytics & Intelligence (4 Tools)
17. **`get_sales_report`**: Generates custom date-range sales & revenue reports (gross sales, AOV, order status breakdown, top-selling items).
18. **`get_best_selling_products`**: Ranks top products by volume and revenue.
19. **`get_store_metrics`**: Summary analytics: total revenue, order count, active order breakdown.
20. **`export_orders_csv`**: Exports orders within a date range to CSV for spreadsheets and accounting.

### ⚙️ 4. Store Operations & Availability (3 Tools)
21. **`set_store_availability`**: Pauses or resumes incoming customer app orders (Open/Closed toggle with custom public reason).
22. **`get_store_settings`**: Reads delivery radius (km), minimum order value (₹), and store open/closed status.
23. **`update_store_settings`**: Configures delivery radius, minimum order amount, and store open/closed status.

---

## 🚀 Running Locally

```bash
cd gstore-mcp-server
npm install
npm run build
npm start
```

Server runs on `http://localhost:3000/mcp`.

---

## 🌐 Deploying with 1-Click Free HTTPS (for Gemini Spark)

### Option A: Render (Free Web Service)
1. Push to your GitHub repository (`main`).
2. Go to [dashboard.render.com](https://dashboard.render.com) -> **New Web Service**.
3. Set **Root Directory** to `gstore-mcp-server`.
4. Set **Build Command** to `npm install && npm run build`.
5. Set **Start Command** to `npm start`.
6. Add Environment Variables:
   - `APPSYNC_ENDPOINT`: `https://gosrh7asubcb3i2qznf6niewd4.appsync-api.us-east-1.amazonaws.com/graphql`
   - `APPSYNC_API_KEY`: `da2-nk6kuao7yrcplhiytlhykr62qq`
   - `CLOUDINARY_CLOUD_NAME`: `k1lw675z`
   - `CLOUDINARY_API_KEY`: `498889461713286`
   - `CLOUDINARY_API_SECRET`: `8SR-robZhuJf-5ehvJTFCscCatY`
7. Copy your deployed URL: `https://your-service.onrender.com/mcp` and paste it into Gemini Spark.

# G-Store MCP Server for Google Gemini Spark

This server implements the **Model Context Protocol (MCP)** to allow **Google Gemini** to directly manage your **G-Store (Rice & Grocery Mart)** AWS AppSync & DynamoDB cloud backend.

---

## 🛠️ Available MCP Tools

### 📦 Catalog & Multi-Variant Management
1. **`list_products`**: Lists all products, categories, pack sizes, selling prices, MRPs, and stock status.
2. **`get_product_by_id`**: Fetches full details for a product by ID.
3. **`create_product`**: Creates a new product, automatically uploading image to Cloudinary CDN (`ricemart_products`).
4. **`update_product_details`**: Updates product name, Telugu name, brand, category, description, image, or listing visibility (`isListed`).
5. **`add_product_variant`**: Adds a new weight or volume size variant (e.g. 5kg, 10kg, 26kg, 500ml, 1L) to an existing product with its own price, MRP, and stock.
6. **`delete_product_variant`**: Removes a specific size variant from a product.
7. **`update_product_price_or_stock`**: Updates the price or stock count for a specific product variant.
8. **`bulk_update_stock_or_prices`**: Batch updates stock or prices across multiple products/variants in a single call (e.g., for wholesale truck arrivals).
9. **`delete_product`**: Removes a product completely from the catalog.
10. **`get_low_stock_alerts`**: Lists all products running low on stock (< threshold units/bags).
11. **`get_best_selling_products`**: Ranks top products by volume and revenue.

### 🛍️ Orders & Store Operations
12. **`list_orders`**: Lists customer orders with status filtering (`PENDING`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`).
13. **`search_customer_orders`**: Looks up order history and lifetime spend by phone number or name.
14. **`update_order_status`**: Advances fulfillment status.
15. **`get_store_metrics`**: Computes total sales revenue, order counts, and active order breakdown.
16. **`get_store_settings`**: Reads delivery radius (km) and minimum order value (₹).
17. **`update_store_settings`**: Configures delivery radius and minimum order value.

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

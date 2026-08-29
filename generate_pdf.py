#!/usr/bin/env python3
"""
Generates a comprehensive, professional PDF documentation manual for all 23 G-Store MCP Server functions.
"""

import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.units import inch
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_header_footer(num_pages)
            canvas.Canvas.showPage(self)
        canvas.Canvas.save(self)

    def draw_header_footer(self, page_count):
        self.saveState()
        if self._pageNumber > 1:
            # Header
            self.setFont("Helvetica-Bold", 8)
            self.setFillColor(colors.HexColor("#064E3B"))
            self.drawString(40, 760, "G-STORE MCP SERVER — 23 FUNCTIONS REFERENCE MANUAL")
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#64748B"))
            self.drawRightString(572, 760, "Google Gemini & AWS AppSync")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(40, 752, 572, 752)

            # Footer
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(40, 45, 572, 45)
            self.setFont("Helvetica", 8)
            self.setFillColor(colors.HexColor("#64748B"))
            self.drawString(40, 32, "G-Store Rice & Kirana Mart, Rajam (A.P.) | Confidential Store Operations")
            self.drawRightString(572, 32, f"Page {self._pageNumber} of {page_count}")
        self.restoreState()

def build_pdf(filename):
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=40,
        rightMargin=40,
        topMargin=50,
        bottomMargin=55
    )

    styles = getSampleStyleSheet()
    
    # Custom Palette
    c_primary = colors.HexColor("#064E3B")    # Dark Emerald
    c_secondary = colors.HexColor("#0D9488")  # Teal
    c_accent = colors.HexColor("#D97706")     # Amber Gold
    c_dark = colors.HexColor("#0F172A")       # Slate 900
    c_gray = colors.HexColor("#475569")       # Slate 600
    c_light_bg = colors.HexColor("#F8FAFC")   # Slate 50
    c_card_bg = colors.HexColor("#ECFDF5")    # Mint 50
    c_border = colors.HexColor("#E2E8F0")

    # Typography Styles
    title_style = ParagraphStyle(
        'CoverTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=28,
        textColor=c_primary,
        alignment=1 # Center
    )

    subtitle_style = ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=12,
        leading=16,
        textColor=c_secondary,
        alignment=1
    )

    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=19,
        textColor=c_primary,
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=15,
        textColor=colors.HexColor("#047857"),
        spaceBefore=10,
        spaceAfter=4,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13,
        textColor=c_dark
    )

    bold_body_style = ParagraphStyle(
        'BoldBody_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9,
        leading=13,
        textColor=c_dark
    )

    code_style = ParagraphStyle(
        'Code_Custom',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8,
        leading=11,
        textColor=colors.HexColor("#0F172A")
    )

    table_header_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=11,
        textColor=colors.white
    )

    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=11,
        textColor=c_dark
    )

    prompt_style = ParagraphStyle(
        'PromptStyle',
        parent=styles['Normal'],
        fontName='Helvetica-Oblique',
        fontSize=8.5,
        leading=12,
        textColor=colors.HexColor("#1E293B")
    )

    story = []

    # ================= COVER / HEADER BANNER =================
    story.append(Spacer(1, 10))
    story.append(Paragraph("🏪 G-STORE RAJAM", title_style))
    story.append(Spacer(1, 4))
    story.append(Paragraph("Model Context Protocol (MCP) Server — Complete 23 Functions Reference", title_style))
    story.append(Spacer(1, 6))
    story.append(Paragraph("Comprehensive Manual & Operational Guide for Google Gemini Spark & AWS AppSync", subtitle_style))
    story.append(Spacer(1, 14))

    meta_table_data = [
        [
            Paragraph("<b>Target Store:</b> G-Store (Rice & Kirana Mart), Rajam, A.P.", table_cell_style),
            Paragraph("<b>Cloud Backend:</b> AWS AppSync GraphQL & DynamoDB", table_cell_style)
        ],
        [
            Paragraph("<b>AI Interface:</b> Google Gemini Spark (MCP Standard 2024-11-05)", table_cell_style),
            Paragraph("<b>Media Storage:</b> Cloudinary CDN (<code>ricemart_products</code>)", table_cell_style)
        ],
        [
            Paragraph("<b>Total Tools:</b> 23 Active Functions", table_cell_style),
            Paragraph("<b>Status:</b> Production Ready & Tested", table_cell_style)
        ]
    ]
    meta_table = Table(meta_table_data, colWidths=[260, 272])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), c_card_bg),
        ('BOX', (0, 0), (-1, -1), 1, c_secondary),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#A7F3D0")),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(meta_table)
    story.append(Spacer(1, 16))

    # ================= SECTION 1: CATALOG & MULTI-VARIANT MANAGEMENT =================
    story.append(Paragraph("1. Catalog & Multi-Variant Management (11 Tools)", h1_style))
    story.append(Paragraph("Enables store managers to create products, update details, generate 600x600 AI packaging photos, manage multi-size pack variants, and restock wholesale batches.", body_style))
    story.append(Spacer(1, 8))

    tools_s1 = [
        {
            "num": "1.1",
            "name": "list_products",
            "summary": "Lists all catalog products with pack sizes, selling prices, MRPs, and stock status. Supports category filtering and listing status toggles.",
            "params": [
                ("category", "string", "No", "Filter by 'Rice Bags', 'Cooking Oils', 'Dals & Pulses', 'Dairy Essentials', 'Spices & Masalas'"),
                ("includeUnlisted", "boolean", "No", "If true, includes hidden/unlisted products (default: false)")
            ],
            "prompt": '"Gemini, list all products under Rice Bags with their current prices and stock counts."',
            "example_out": 'Returns count and array of products with formatted variants: 26kg bag @ ₹1450 (MRP ₹1600, Stock: 45).'
        },
        {
            "num": "1.2",
            "name": "get_product_by_id",
            "summary": "Retrieves comprehensive information, all pack size variants, Cloudinary image URLs, and English/Telugu descriptions for a specific product ID.",
            "params": [
                ("productId", "string", "Yes", "The unique product ID (e.g. 'p_rice_sona', 'p_oil_freedom')")
            ],
            "prompt": '"Gemini, show me full product details for product ID p_rice_sona."',
            "example_out": 'Returns product name, Telugu name, Cloudinary image URL, and all available pack variants.'
        },
        {
            "num": "1.3",
            "name": "create_product",
            "summary": "Creates a new product in the G-Store catalog, automatically uploading the image to Cloudinary folder 'ricemart_products' and linking it to AWS AppSync.",
            "params": [
                ("name", "string", "Yes", "English display name (e.g. 'Amul Taaza Toned Milk')"),
                ("category", "string", "Yes", "Category: 'Dairy Essentials', 'Rice Bags', 'Cooking Oils', etc."),
                ("price", "number", "Yes", "Selling price in ₹ (e.g. 34)"),
                ("mrp", "number", "No", "Printed MRP in ₹ (defaults to ~15% above selling price)"),
                ("size", "string", "No", "Weight/volume size (e.g. '500ml', '1L', '26kg', '10kg')"),
                ("stock", "number", "No", "Initial stock count in units/bags (default: 50)"),
                ("description", "string", "No", "English description or brand notes"),
                ("imageUrl", "string", "No", "Source image URL or base64 (auto-uploaded to Cloudinary)")
            ],
            "prompt": '"Gemini, add a new product: Fortune Sunlite Refined Sunflower Oil 1L at ₹125 (MRP 140), stock 40 packets, under Cooking Oils."',
            "example_out": 'Created product p_oil_fortune_sunlite with Cloudinary CDN URL and formatted variant in DynamoDB.'
        },
        {
            "num": "1.4",
            "name": "update_product_details",
            "summary": "Updates product names, Telugu names, brand, category, description, image, or listing visibility without deleting or recreating the item.",
            "params": [
                ("productId", "string", "Yes", "The unique product ID to modify"),
                ("name", "string", "No", "New English display name"),
                ("nameTe", "string", "No", "New Telugu display name (e.g. 'లలిత హెచ్.ఎమ్.టి బియ్యం')"),
                ("brand", "string", "No", "Brand name (e.g. 'Lalitha', 'Heritage', 'Freedom')"),
                ("category", "string", "No", "New category key"),
                ("description", "string", "No", "New English description"),
                ("descriptionTe", "string", "No", "New Telugu description"),
                ("imageUrl", "string", "No", "New image URL (auto-uploaded to Cloudinary)"),
                ("isListed", "boolean", "No", "Set to false to hide item from customer app")
            ],
            "prompt": '"Gemini, update product p_rice_lalitha: set Telugu name to లలిత హెచ్.ఎమ్.టి రైస్ and brand to Lalitha."',
            "example_out": 'Product details updated successfully in AppSync with Telugu name and brand tags.'
        },
        {
            "num": "1.5",
            "name": "generate_and_upload_product_image",
            "summary": "Generates an 8K studio packaging photo on a clean white background using AI, or processes a reference photo taken on a phone, automatically applying 600x600 square padding and uploading to Cloudinary CDN.",
            "params": [
                ("productName", "string", "Yes", "Product name (e.g. 'Lalitha Brand HMT Rice 26kg')"),
                ("productId", "string", "No", "Optional product ID to immediately link the image in AWS"),
                ("referenceImageUrlOrBase64", "string", "No", "Optional phone photo URL or base64 image data"),
                ("customPrompt", "string", "No", "Optional visual details (e.g. 'yellow woven bag with red logo')"),
                ("category", "string", "No", "Product category preset")
            ],
            "prompt": '"Gemini, generate a clean studio packaging image for Lalitha Brand HMT Rice 26kg bag and attach it to p_rice_lalitha."',
            "example_out": 'Generated 600x600 padded image on white background, uploaded to Cloudinary, attached to product.'
        },
        {
            "num": "1.6",
            "name": "add_product_variant",
            "summary": "Adds a new weight/volume size variant (e.g. adding 10kg alongside an existing 26kg bag) with independent price, MRP, and stock quantity under the same listing.",
            "params": [
                ("productId", "string", "Yes", "Product ID to receive the new variant"),
                ("size", "string", "Yes", "Size string (e.g. '10kg', '5kg', '500ml', '1L', '200g')"),
                ("price", "number", "Yes", "Selling price in ₹ for this size"),
                ("mrp", "number", "No", "Printed MRP in ₹"),
                ("stock", "number", "No", "Initial stock count (default: 50)")
            ],
            "prompt": '"Gemini, add a 10kg variant at ₹580 (MRP 650) to Sona Masoori Rice (p_rice_sona) with 30 bags stock."',
            "example_out": 'Added variant 10kg to p_rice_sona. App displays 10kg option in size selector.'
        },
        {
            "num": "1.7",
            "name": "delete_product_variant",
            "summary": "Removes a specific size variant (e.g. deleting the 5kg option) while keeping the product listing and all other sizes intact.",
            "params": [
                ("productId", "string", "Yes", "Product ID"),
                ("variantSize", "string", "Yes", "Size to remove (e.g. '5kg', '500ml')")
            ],
            "prompt": '"Gemini, remove the 5kg size variant from Sona Masoori Rice (p_rice_sona)."',
            "example_out": 'Variant 5kg removed successfully. Product now offers 10kg and 26kg sizes.'
        },
        {
            "num": "1.8",
            "name": "update_product_price_or_stock",
            "summary": "Updates the selling price or stock quantity for an existing variant size in a single instant operation.",
            "params": [
                ("productId", "string", "Yes", "Product ID"),
                ("variantSize", "string", "No", "Target variant size (e.g. '26kg')"),
                ("newPrice", "number", "No", "New selling price in ₹"),
                ("stock", "number", "No", "New absolute stock quantity")
            ],
            "prompt": '"Gemini, change the price of Lalitha HMT Rice 26kg to ₹1480 and set stock to 45."',
            "example_out": 'Variant price updated to ₹1480, stock set to 45 bags.'
        },
        {
            "num": "1.9",
            "name": "bulk_update_stock_or_prices",
            "summary": "Batch updates stock levels (setting absolute count or adding stock increments) and prices across multiple products in one atomic call. Ideal for wholesale truck arrivals.",
            "params": [
                ("updates", "array", "Yes", "List of { productId, variantSize?, newPrice?, stock?, addStock? }")
            ],
            "prompt": '"Gemini, a wholesale truck arrived: add 50 bags to Lalitha HMT (p_rice_lalitha) and add 40 bags to BPT Rice (p_rice_bpt)."',
            "example_out": 'Updated 2 products in batch. Stock incremented by +50 and +40 bags.'
        },
        {
            "num": "1.10",
            "name": "delete_product",
            "summary": "Permanently deletes a product and all of its variants from the store catalog.",
            "params": [
                ("productId", "string", "Yes", "ID of the product to delete")
            ],
            "prompt": '"Gemini, delete product p_rice_old_discontinued from the catalog."',
            "example_out": 'Product deleted successfully from AppSync DynamoDB table.'
        },
        {
            "num": "1.11",
            "name": "get_low_stock_alerts",
            "summary": "Identifies and ranks all products running low on inventory below a given threshold units/bags so the store owner knows what to reorder.",
            "params": [
                ("threshold", "number", "No", "Stock threshold to trigger alert (default: 10 units)")
            ],
            "prompt": '"Gemini, show me all items running low on stock below 15 units."',
            "example_out": 'Found 4 low-stock items (e.g. Heritage Milk: 3 packets left, Freedom Oil: 6 bottles left).'
        }
    ]

    for t in tools_s1:
        story.append(render_tool_block(t, h2_style, bold_body_style, body_style, table_header_style, table_cell_style, prompt_style, c_primary, c_light_bg, c_border))
        story.append(Spacer(1, 10))

    story.append(PageBreak())

    # ================= SECTION 2: ORDERS & OMNICHANNEL FULFILLMENT =================
    story.append(Paragraph("2. Orders & Omnichannel Fulfillment (5 Tools)", h1_style))
    story.append(Paragraph("Enables recording phone/walk-in orders, auto-deducting inventory, generating printable WhatsApp invoices, and tracking delivery status.", body_style))
    story.append(Spacer(1, 8))

    tools_s2 = [
        {
            "num": "2.1",
            "name": "create_order",
            "summary": "Records counter, phone, or WhatsApp customer orders directly into DynamoDB/AppSync. Automatically verifies current catalog prices, computes totals, and deducts inventory stock.",
            "params": [
                ("customerName", "string", "Yes", "Customer full name (e.g. 'Ramesh', 'Sita Devi')"),
                ("customerPhone", "string", "Yes", "Customer 10-digit mobile number"),
                ("deliveryAddress", "string", "Yes", "House no, landmark, or street in Rajam"),
                ("items", "array", "Yes", "List of { productIdOrName, variantSize?, quantity, price? }"),
                ("status", "string", "No", "Initial status: 'PENDING', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED' (default: PENDING)"),
                ("deliveryFee", "number", "No", "Delivery fee in ₹ (default: 0)")
            ],
            "prompt": '"Gemini, create a COD order for Ramesh (Phone: 9876543210) at D.No 4-12, Near SBI Bank, Rajam: 1 bag Lalitha HMT Rice 26kg and 2 packets Freedom Sunflower Oil 1L."',
            "example_out": 'Order #G-1786896453001-823 recorded. Total: ₹1,690. Stock auto-deducted.'
        },
        {
            "num": "2.2",
            "name": "generate_order_invoice",
            "summary": "Generates a clean, professional text receipt and packing slip formatted for printing or copying directly into WhatsApp for customers and delivery boys.",
            "params": [
                ("orderId", "string", "Yes", "The unique order ID (e.g. 'G-1786896453001-823')")
            ],
            "prompt": '"Gemini, generate a WhatsApp receipt and bill for order G-1786896453001-823."',
            "example_out": 'Generates formatted bill: Store name, Order ID, Customer, Address, Itemized totals, Delivery fee, and Total due.'
        },
        {
            "num": "2.3",
            "name": "list_orders",
            "summary": "Fetches recent customer orders from AWS AppSync with status filtering (PENDING, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED).",
            "params": [
                ("status", "string", "No", "Filter: 'PENDING', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'"),
                ("limit", "number", "No", "Maximum number of orders to return (default: 20)")
            ],
            "prompt": '"Gemini, list all pending orders that need packing right now."',
            "example_out": 'Returns list of 5 pending orders with customer addresses and item lists.'
        },
        {
            "num": "2.4",
            "name": "search_customer_orders",
            "summary": "Searches customer order history, past delivery addresses, and lifetime spending by customer phone number or name.",
            "params": [
                ("query", "string", "Yes", "Customer mobile number (e.g. '9704173515') or name")
            ],
            "prompt": '"Gemini, search order history and total spend for customer phone 9704173515."',
            "example_out": 'Found 8 past orders for Sunny. Lifetime spend: ₹14,200. Preferred address: Main Road, Rajam.'
        },
        {
            "num": "2.5",
            "name": "update_order_status",
            "summary": "Advances an order along the fulfillment pipeline (PENDING -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED -> CANCELLED).",
            "params": [
                ("orderId", "string", "Yes", "The order ID to update"),
                ("newStatus", "string", "Yes", "One of: 'PENDING', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'")
            ],
            "prompt": '"Gemini, mark order G-1786896453001-823 as OUT_FOR_DELIVERY."',
            "example_out": 'Order status updated to OUT_FOR_DELIVERY in AWS AppSync.'
        }
    ]

    for t in tools_s2:
        story.append(render_tool_block(t, h2_style, bold_body_style, body_style, table_header_style, table_cell_style, prompt_style, c_primary, c_light_bg, c_border))
        story.append(Spacer(1, 10))

    story.append(PageBreak())

    # ================= SECTION 3: SALES ANALYTICS & INTELLIGENCE =================
    story.append(Paragraph("3. Sales Analytics & Intelligence (4 Tools)", h1_style))
    story.append(Paragraph("Provides financial intelligence, date-range revenue analytics, best-selling product rankings, and CSV export for accounting.", body_style))
    story.append(Spacer(1, 8))

    tools_s3 = [
        {
            "num": "3.1",
            "name": "get_sales_report",
            "summary": "Generates a detailed sales and revenue report for any custom date range (e.g. today, this week, or August 2026). Computes gross revenue, delivered/pending/cancelled counts, average order value, daily breakdown, and top-selling products.",
            "params": [
                ("startDate", "string", "No", "Start date (YYYY-MM-DD, e.g. '2026-08-01')"),
                ("endDate", "string", "No", "End date (YYYY-MM-DD, e.g. '2026-08-29')"),
                ("category", "string", "No", "Optional category filter")
            ],
            "prompt": '"Gemini, generate a sales report for this month from 2026-08-01 to 2026-08-29."',
            "example_out": 'Total Gross Revenue: ₹1,48,500 across 94 orders. AOV: ₹1,580. Top product: Lalitha HMT 26kg (42 bags sold).'
        },
        {
            "num": "3.2",
            "name": "export_orders_csv",
            "summary": "Exports orders within a date range to structured CSV format suitable for opening in Microsoft Excel or Tally for accounting and GST audit.",
            "params": [
                ("startDate", "string", "No", "Start date filter (YYYY-MM-DD)"),
                ("endDate", "string", "No", "End date filter (YYYY-MM-DD)"),
                ("status", "string", "No", "Status filter: 'DELIVERED', 'PENDING', 'ALL'")
            ],
            "prompt": '"Gemini, export all delivered orders for this month to a CSV file for my accountant."',
            "example_out": 'Returns CSV data: OrderID, Date, CustomerName, Phone, Status, ItemCount, Subtotal, DeliveryFee, Total.'
        },
        {
            "num": "3.3",
            "name": "get_best_selling_products",
            "summary": "Ranks top products in G-Store by total bags/units sold and revenue generated from completed orders.",
            "params": [
                ("limit", "number", "No", "Number of top products to return (default: 5)")
            ],
            "prompt": '"Gemini, what are our top 5 best-selling products this month?"',
            "example_out": '1. Lalitha HMT Rice (₹60,900), 2. Sona Masoori Rice (₹42,000), 3. Freedom Oil (₹18,400).'
        },
        {
            "num": "3.4",
            "name": "get_store_metrics",
            "summary": "Computes high-level lifetime store analytics: total orders placed, gross lifetime revenue, pending orders, and recent order volume.",
            "params": [],
            "prompt": '"Gemini, give me a quick summary of overall store metrics."',
            "example_out": 'Lifetime Revenue: ₹4,82,000 | Total Orders: 312 | Pending Orders: 4 | Average Order: ₹1,545.'
        }
    ]

    for t in tools_s3:
        story.append(render_tool_block(t, h2_style, bold_body_style, body_style, table_header_style, table_cell_style, prompt_style, c_primary, c_light_bg, c_border))
        story.append(Spacer(1, 10))

    story.append(PageBreak())

    # ================= SECTION 4: STORE OPERATIONS & SETTINGS =================
    story.append(Paragraph("4. Store Operations & Availability (3 Tools)", h1_style))
    story.append(Paragraph("Provides real-time operational control to pause orders during heavy rains, update delivery radius, and manage minimum order amounts.", body_style))
    story.append(Spacer(1, 8))

    tools_s4 = [
        {
            "num": "4.1",
            "name": "set_store_availability",
            "summary": "Pauses or resumes incoming customer app orders by setting the store to Open or Closed with a public notice (e.g. heavy rain in Rajam, power cut, festival holiday, inventory counting).",
            "params": [
                ("isOpen", "boolean", "Yes", "Set true to accept orders, false to pause orders"),
                ("closingReason", "string", "No", "Notice shown to customers (e.g. 'Heavy rain in Rajam — deliveries paused until 4 PM')")
            ],
            "prompt": '"Gemini, it is raining heavily in Rajam right now. Please close store orders with reason: Heavy rain in Rajam — delivery paused until 4:30 PM."',
            "example_out": 'Store status set to CLOSED in AppSync. Customer app displays the notice banner.'
        },
        {
            "num": "4.2",
            "name": "get_store_settings",
            "summary": "Reads active store configuration parameters from AWS AppSync including delivery radius (km), minimum order value (₹), and store open/closed status.",
            "params": [],
            "prompt": '"Gemini, what are our current delivery radius and minimum order settings?"',
            "example_out": 'Minimum Order: ₹150 | Delivery Radius: 10.0 km | Status: 🟢 OPEN.'
        },
        {
            "num": "4.3",
            "name": "update_store_settings",
            "summary": "Configures store delivery radius (in km), minimum order amount (in ₹), or store availability status.",
            "params": [
                ("minimumOrderAmount", "number", "No", "New minimum order value in ₹ (e.g. 200)"),
                ("deliveryRadiusKm", "number", "No", "New delivery radius in km (e.g. 12.5)"),
                ("isStoreOpen", "boolean", "No", "Store open/closed boolean"),
                ("closingReason", "string", "No", "Reason text if closed")
            ],
            "prompt": '"Gemini, update store settings: set minimum order amount to ₹200 and delivery radius to 12 km."',
            "example_out": 'Store settings updated in sys_config: Min order ₹200, Radius 12 km.'
        }
    ]

    for t in tools_s4:
        story.append(render_tool_block(t, h2_style, bold_body_style, body_style, table_header_style, table_cell_style, prompt_style, c_primary, c_light_bg, c_border))
        story.append(Spacer(1, 10))

    # ================= QUICK REFERENCE CHEAT SHEET =================
    story.append(Spacer(1, 8))
    story.append(Paragraph("5. Daily Operational Prompt Cheat Sheet", h1_style))
    
    cheat_data = [
        [
            Paragraph("<b>Scenario / Task</b>", table_header_style),
            Paragraph("<b>Recommended Gemini Voice / Chat Prompt</b>", table_header_style),
            Paragraph("<b>Primary MCP Tool</b>", table_header_style)
        ],
        [
            Paragraph("Wholesale Truck Arrival", table_cell_style),
            Paragraph('"Add 50 bags stock to Lalitha HMT 26kg and 30 bags to BPT Rice 26kg"', prompt_style),
            Paragraph("<code>bulk_update_stock_or_prices</code>", table_cell_style)
        ],
        [
            Paragraph("Add New Pack Size", table_cell_style),
            Paragraph('"Add 10kg variant at ₹580 to Sona Masoori Rice with 40 bags stock"', prompt_style),
            Paragraph("<code>add_product_variant</code>", table_cell_style)
        ],
        [
            Paragraph("Generate Studio Photo", table_cell_style),
            Paragraph('"Generate clean studio 600x600 packaging photo for Lalitha Rice 26kg bag"', prompt_style),
            Paragraph("<code>generate_and_upload_product_image</code>", table_cell_style)
        ],
        [
            Paragraph("Phone / Walk-in Order", table_cell_style),
            Paragraph('"Create order for Ramesh (9876543210) for 1 bag Lalitha Rice and 2 Freedom Oils"', prompt_style),
            Paragraph("<code>create_order</code>", table_cell_style)
        ],
        [
            Paragraph("WhatsApp Receipt", table_cell_style),
            Paragraph('"Generate WhatsApp bill for order G-1786896453001-823"', prompt_style),
            Paragraph("<code>generate_order_invoice</code>", table_cell_style)
        ],
        [
            Paragraph("Heavy Rain / Pause", table_cell_style),
            Paragraph('"Close store orders: Heavy rain in Rajam — delivery paused until 4 PM"', prompt_style),
            Paragraph("<code>set_store_availability</code>", table_cell_style)
        ],
        [
            Paragraph("Monthly Sales Report", table_cell_style),
            Paragraph('"Give me sales report from 2026-08-01 to 2026-08-29 with daily breakdown"', prompt_style),
            Paragraph("<code>get_sales_report</code>", table_cell_style)
        ],
        [
            Paragraph("Excel CSV Export", table_cell_style),
            Paragraph('"Export this month\'s delivered orders to CSV for accounting"', prompt_style),
            Paragraph("<code>export_orders_csv</code>", table_cell_style)
        ]
    ]

    cheat_table = Table(cheat_data, colWidths=[120, 270, 142])
    cheat_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_primary),
        ('BOX', (0, 0), (-1, -1), 1, c_secondary),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, c_border),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, c_light_bg]),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 6),
        ('RIGHTPADDING', (0, 0), (-1, -1), 6),
    ]))
    story.append(cheat_table)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"PDF generated successfully: {filename}")

def render_tool_block(t, h2_style, bold_body_style, body_style, table_header_style, table_cell_style, prompt_style, c_primary, c_light_bg, c_border):
    elements = []
    
    # Tool Title
    title_text = f"<b>{t['num']}  <code>{t['name']}</code></b>"
    elements.append(Paragraph(title_text, h2_style))
    elements.append(Paragraph(t['summary'], body_style))
    elements.append(Spacer(1, 4))

    # Parameters Table (if any)
    if t['params']:
        param_rows = [
            [
                Paragraph("<b>Parameter</b>", table_header_style),
                Paragraph("<b>Type</b>", table_header_style),
                Paragraph("<b>Req</b>", table_header_style),
                Paragraph("<b>Description</b>", table_header_style)
            ]
        ]
        for p in t['params']:
            param_rows.append([
                Paragraph(f"<code>{p[0]}</code>", table_cell_style),
                Paragraph(p[1], table_cell_style),
                Paragraph(f"<b>{p[2]}</b>", table_cell_style),
                Paragraph(p[3], table_cell_style)
            ])
        
        param_table = Table(param_rows, colWidths=[110, 45, 35, 342])
        param_table.setStyle(TableStyle([
            ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#065F46")),
            ('BOX', (0, 0), (-1, -1), 0.5, c_border),
            ('INNERGRID', (0, 0), (-1, -1), 0.5, c_border),
            ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, c_light_bg]),
            ('TOPPADDING', (0, 0), (-1, -1), 3),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
            ('LEFTPADDING', (0, 0), (-1, -1), 5),
            ('RIGHTPADDING', (0, 0), (-1, -1), 5),
        ]))
        elements.append(param_table)
        elements.append(Spacer(1, 4))

    # Example Prompt Box
    example_data = [
        [
            Paragraph("<b>Example Gemini Prompt:</b>", bold_body_style),
            Paragraph(t['prompt'], prompt_style)
        ],
        [
            Paragraph("<b>Result Output:</b>", bold_body_style),
            Paragraph(t['example_out'], body_style)
        ]
    ]
    ex_table = Table(example_data, colWidths=[130, 402])
    ex_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#F0FDF4")),
        ('BOX', (0, 0), (-1, -1), 0.5, colors.HexColor("#86EFAC")),
        ('INNERGRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#DCFCE7")),
        ('TOPPADDING', (0, 0), (-1, -1), 3),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 3),
        ('LEFTPADDING', (0, 0), (-1, -1), 5),
        ('RIGHTPADDING', (0, 0), (-1, -1), 5),
    ]))
    elements.append(ex_table)
    
    return KeepTogether(elements)

if __name__ == "__main__":
    out_pdf = "/Users/saisunkari/antigravity/G-Store/G-Store_MCP_Server_23_Functions_Manual.pdf"
    build_pdf(out_pdf)

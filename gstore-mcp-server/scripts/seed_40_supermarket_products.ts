import { executeGraphQL } from "../src/appsync.js";
import dotenv from "dotenv";

dotenv.config();

interface ProductVariantSeed {
  unitVal: string;
  unitType: string;
  price: number;
  mrp: number;
  stock: number;
}

interface ProductSeed {
  id: string;
  name: string;
  brand: string;
  category: string;
  descriptionEn: string;
  imageUrl: string;
  variants: ProductVariantSeed[];
}

const products40: ProductSeed[] = [
  // ==========================================
  // 1. RICE & GRAINS (c_rice)
  // ==========================================
  {
    id: "p_rice_lalitha_hmt",
    name: "Lalitha Brand HMT Premium Rice",
    brand: "Lalitha",
    category: "c_rice",
    descriptionEn: "Lalitha Brand HMT Rice is naturally aged for 12+ months, ensuring slender, non-sticky grains that expand beautifully upon cooking.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "26", unitType: "kg", price: 1480, mrp: 1650, stock: 50 },
      { unitVal: "10", unitType: "kg", price: 590, mrp: 660, stock: 30 },
      { unitVal: "5", unitType: "kg", price: 300, mrp: 340, stock: 25 },
    ],
  },
  {
    id: "p_rice_sona_masoori",
    name: "Kurnool Sona Masoori Raw Rice",
    brand: "Bell Brand",
    category: "c_rice",
    descriptionEn: "Authentic Kurnool Sona Masoori raw rice, lightweight and aromatic with low starch content. Perfect for daily South Indian meals.",
    imageUrl: "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "26", unitType: "kg", price: 1420, mrp: 1580, stock: 40 },
      { unitVal: "10", unitType: "kg", price: 560, mrp: 620, stock: 30 },
      { unitVal: "5", unitType: "kg", price: 290, mrp: 320, stock: 20 },
    ],
  },
  {
    id: "p_rice_india_gate_basmati",
    name: "India Gate Rozzana Basmati Rice",
    brand: "India Gate",
    category: "c_rice",
    descriptionEn: "India Gate Rozzana Basmati rice brings royal aroma and distinct long slender grains to your special weekend dinners and fried rice dishes.",
    imageUrl: "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "5", unitType: "kg", price: 460, mrp: 525, stock: 30 },
      { unitVal: "1", unitType: "kg", price: 98, mrp: 115, stock: 50 },
    ],
  },
  {
    id: "p_flour_aashirvaad_atta",
    name: "Aashirvaad Superior MP Sharbati Whole Wheat Atta",
    brand: "Aashirvaad",
    category: "c_rice",
    descriptionEn: "100% pure whole wheat grain atta made with traditional stone chakki grinding for extra soft rotis with 0% maida.",
    imageUrl: "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "10", unitType: "kg", price: 440, mrp: 495, stock: 45 },
      { unitVal: "5", unitType: "kg", price: 230, mrp: 260, stock: 40 },
      { unitVal: "1", unitType: "kg", price: 48, mrp: 55, stock: 60 },
    ],
  },

  // ==========================================
  // 2. COOKING OILS & GHEE (c_oil)
  // ==========================================
  {
    id: "p_oil_freedom_sunflower",
    name: "Freedom Refined Sunflower Oil",
    brand: "Freedom",
    category: "c_oil",
    descriptionEn: "Freedom refined sunflower oil with Vitamins A, D & E. Light, non-greasy and healthy for heart-conscious cooking.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 128, mrp: 155, stock: 60 },
      { unitVal: "5", unitType: "L", price: 630, mrp: 760, stock: 25 },
    ],
  },
  {
    id: "p_oil_fortune_sunflower",
    name: "Fortune Sunlite Refined Sunflower Oil",
    brand: "Fortune",
    category: "c_oil",
    descriptionEn: "Fortune Sunlite Refined Sunflower Oil is enriched with natural nutrients and high smoke point for crispy deep frying.",
    imageUrl: "https://images.unsplash.com/photo-1620706857370-e1b9770e8bb1?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 132, mrp: 160, stock: 50 },
      { unitVal: "5", unitType: "L", price: 645, mrp: 780, stock: 20 },
    ],
  },
  {
    id: "p_oil_grb_ghee",
    name: "GRB Pure Cow Ghee Jar",
    brand: "GRB",
    category: "c_oil",
    descriptionEn: "Traditional granulated aroma GRB pure cow ghee. Made from pure cream for authentic taste in sweets and dosas.",
    imageUrl: "https://images.unsplash.com/photo-1631451095765-2c91616fc9e6?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "ml", price: 345, mrp: 385, stock: 40 },
      { unitVal: "200", unitType: "ml", price: 145, mrp: 165, stock: 50 },
      { unitVal: "1", unitType: "L", price: 680, mrp: 750, stock: 20 },
    ],
  },
  {
    id: "p_oil_fortune_mustard",
    name: "Fortune Kachi Ghani Pure Mustard Oil",
    brand: "Fortune",
    category: "c_oil",
    descriptionEn: "Strong pungent cold-pressed mustard oil for traditional Indian pickles and pungent curries.",
    imageUrl: "https://images.unsplash.com/photo-1546554137-f86b9593a222?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 145, mrp: 175, stock: 35 },
    ],
  },

  // ==========================================
  // 3. DALS, PULSES & STAPLES (c_dal)
  // ==========================================
  {
    id: "p_dal_tata_toor",
    name: "Tata Sampann Unpolished Toor Dal (Kandi Pappu)",
    brand: "Tata Sampann",
    category: "c_dal",
    descriptionEn: "Tata Sampann unpolished Toor Dal retains natural dietary fiber, protein, and authentic taste for daily sambar and dal tadka.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 168, mrp: 195, stock: 50 },
      { unitVal: "500", unitType: "g", price: 88, mrp: 102, stock: 40 },
    ],
  },
  {
    id: "p_dal_minapa_gundlu",
    name: "G-Store Select Premium Minapa Gundlu (Whole Urad)",
    brand: "G-Store Select",
    category: "c_dal",
    descriptionEn: "Top-quality whole white unpolished Urad Dal. High batter yield, perfect for fluffy idlis and crispy golden medu vadas.",
    imageUrl: "https://images.unsplash.com/photo-1515543237350-b3eea1ec8082?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 142, mrp: 165, stock: 45 },
      { unitVal: "500", unitType: "g", price: 74, mrp: 86, stock: 40 },
    ],
  },
  {
    id: "p_dal_tata_moong",
    name: "Tata Sampann Yellow Moong Dal (Pesara Pappu)",
    brand: "Tata Sampann",
    category: "c_dal",
    descriptionEn: "Nutritious and easily digestible unpolished yellow split moong dal for khichdi, pesara pappu, and healthy soups.",
    imageUrl: "https://images.unsplash.com/photo-1543353071-873f17a7a088?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 135, mrp: 155, stock: 40 },
      { unitVal: "500", unitType: "g", price: 70, mrp: 82, stock: 35 },
    ],
  },
  {
    id: "p_staple_madhur_sugar",
    name: "Madhur Pure & Hygienic Refined Sugar",
    brand: "Madhur",
    category: "c_dal",
    descriptionEn: "100% sulphur-free, sparkling white, untouched by hand pure crystal sugar for daily tea, coffee, and traditional sweets.",
    imageUrl: "https://images.unsplash.com/photo-1581441363689-1f3c3c414635?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "5", unitType: "kg", price: 235, mrp: 265, stock: 35 },
      { unitVal: "1", unitType: "kg", price: 48, mrp: 55, stock: 60 },
    ],
  },

  // ==========================================
  // 4. DAIRY ESSENTIALS (c_dairy)
  // ==========================================
  {
    id: "p_dairy_heritage_toned_milk",
    name: "Heritage Daily Fresh Toned Milk Pouch",
    brand: "Heritage",
    category: "c_dairy",
    descriptionEn: "Pasteurized, homogenized toned milk (3.0% Fat, 8.5% SNF). Daily fresh supply for tea, coffee, and daily calcium.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "ml", price: 29, mrp: 30, stock: 100 },
      { unitVal: "1", unitType: "L", price: 58, mrp: 60, stock: 50 },
    ],
  },
  {
    id: "p_dairy_heritage_full_cream",
    name: "Heritage Special Full Cream Gold Milk",
    brand: "Heritage",
    category: "c_dairy",
    descriptionEn: "Rich & thick full cream milk (6.0% Fat, 9.0% SNF). Perfect for thick creamy curd, homemade paneer, and rich kheer.",
    imageUrl: "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "ml", price: 36, mrp: 37, stock: 80 },
    ],
  },
  {
    id: "p_dairy_amul_butter",
    name: "Amul Pasteurized Salted Butter",
    brand: "Amul",
    category: "c_dairy",
    descriptionEn: "The classic Utterly Butterly Delicious Amul Butter, made from fresh cream. Perfect spread on hot parathas, toast, and dosas.",
    imageUrl: "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 275, mrp: 285, stock: 40 },
      { unitVal: "100", unitType: "g", price: 58, mrp: 60, stock: 60 },
    ],
  },
  {
    id: "p_dairy_amul_paneer",
    name: "Amul Fresh Malai Paneer Block",
    brand: "Amul",
    category: "c_dairy",
    descriptionEn: "Soft, rich, and protein-packed fresh malai paneer. Retains shape and soft melt-in-mouth texture when cooked.",
    imageUrl: "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "200", unitType: "g", price: 92, mrp: 95, stock: 45 },
      { unitVal: "500", unitType: "g", price: 220, mrp: 235, stock: 25 },
    ],
  },

  // ==========================================
  // 5. SPICES & MASALAS (c_spices)
  // ==========================================
  {
    id: "p_spice_aashirvaad_chilli",
    name: "Aashirvaad Special Guntur Chilli Powder",
    brand: "Aashirvaad",
    category: "c_spices",
    descriptionEn: "Made from sun-dried Guntur chillies, providing bright natural red color and fiery pungent taste without artificial additives.",
    imageUrl: "https://images.unsplash.com/photo-1627054234036-74521639d675?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 185, mrp: 210, stock: 40 },
      { unitVal: "200", unitType: "g", price: 78, mrp: 90, stock: 50 },
    ],
  },
  {
    id: "p_spice_everest_turmeric",
    name: "Everest Pure Turmeric Powder (Haldi)",
    brand: "Everest",
    category: "c_spices",
    descriptionEn: "High curcumin content Salem turmeric roots ground to fine golden powder for immune health and rich curry color.",
    imageUrl: "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 140, mrp: 160, stock: 35 },
      { unitVal: "200", unitType: "g", price: 60, mrp: 70, stock: 45 },
    ],
  },
  {
    id: "p_spice_everest_garam_masala",
    name: "Everest Royal Garam Masala Blend",
    brand: "Everest",
    category: "c_spices",
    descriptionEn: "Authentic blend of 13 roasted whole spices including cinnamon, cloves, and cardamom for rich biryani aroma.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "100", unitType: "g", price: 78, mrp: 90, stock: 50 },
      { unitVal: "50", unitType: "g", price: 42, mrp: 48, stock: 60 },
    ],
  },
  {
    id: "p_staple_tata_salt",
    name: "Tata Salt Vacuum Evaporated Iodized Salt",
    brand: "Tata",
    category: "c_spices",
    descriptionEn: "Desh Ka Namak. Purity guaranteed vacuum evaporated iodized salt essential for balanced iodine intake and daily cooking.",
    imageUrl: "https://images.unsplash.com/photo-1518110925495-5fe2fda0442c?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 27, mrp: 30, stock: 100 },
    ],
  },

  // ==========================================
  // 6. DRY FRUITS & NUTS (c_dryfruits)
  // ==========================================
  {
    id: "p_dryfruit_cashew_w240",
    name: "G-Store W240 Premium Whole Cashew Nuts (Kaju)",
    brand: "G-Store Select",
    category: "c_dryfruits",
    descriptionEn: "Top-grade W240 whole crunchy cashews from Palasa. Perfect for roasting with ghee, garnishing sweets, and daily snacking.",
    imageUrl: "https://images.unsplash.com/photo-1536591375315-1b8368155986?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 475, mrp: 550, stock: 25 },
      { unitVal: "250", unitType: "g", price: 245, mrp: 285, stock: 35 },
    ],
  },
  {
    id: "p_dryfruit_almonds_california",
    name: "California Select Almonds (Badam Giri)",
    brand: "Royal Select",
    category: "c_dryfruits",
    descriptionEn: "100% natural, sweet California almonds. Ideal for morning soaking, badam milk, and energy-packed daily snacking.",
    imageUrl: "https://images.unsplash.com/photo-1508061252445-b95ce682619d?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 440, mrp: 510, stock: 30 },
      { unitVal: "250", unitType: "g", price: 225, mrp: 265, stock: 40 },
    ],
  },
  {
    id: "p_dryfruit_raisins_kismis",
    name: "Indian Golden Long Raisins (Kismis / Endu Draksha)",
    brand: "Royal Select",
    category: "c_dryfruits",
    descriptionEn: "Seedless long golden kismis with natural sweetness. Packed with iron, antioxidants, and instant energy.",
    imageUrl: "https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 195, mrp: 230, stock: 30 },
      { unitVal: "250", unitType: "g", price: 105, mrp: 125, stock: 40 },
    ],
  },
  {
    id: "p_dryfruit_lion_dates",
    name: "Lion Dates (Original Arabian Dates / Kharjooram)",
    brand: "Lion Dates",
    category: "c_dryfruits",
    descriptionEn: "Lion Dates are handpicked desert dates rich in iron and dietary fiber. Boosts hemoglobin and daily vitality.",
    imageUrl: "https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 160, mrp: 180, stock: 40 },
      { unitVal: "200", unitType: "g", price: 70, mrp: 80, stock: 50 },
    ],
  },

  // ==========================================
  // 7. SNACKS & NAMKEEN (c_snacks)
  // ==========================================
  {
    id: "p_snack_haldirams_bhujia",
    name: "Haldiram's Nagpur Aloo Bhujia (Crispy Sev)",
    brand: "Haldiram's",
    category: "c_snacks",
    descriptionEn: "The legendary crispy spicy aloo bhujia with mint and spices. Perfect crunchy accompaniment with tea and chaat.",
    imageUrl: "https://images.unsplash.com/photo-1599490659213-e2b9527bd087?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "400", unitType: "g", price: 115, mrp: 130, stock: 40 },
      { unitVal: "200", unitType: "g", price: 62, mrp: 70, stock: 50 },
      { unitVal: "1", unitType: "kg", price: 270, mrp: 310, stock: 20 },
    ],
  },
  {
    id: "p_snack_kurkure_masala",
    name: "Kurkure Masala Munch (Crispy Namkeen)",
    brand: "Kurkure",
    category: "c_snacks",
    descriptionEn: "Crispy, crunchy, tangy Indian puffed corn snack made with kitchen ingredients and authentic masalas.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "90", unitType: "g", price: 20, mrp: 20, stock: 80 },
      { unitVal: "180", unitType: "g", price: 38, mrp: 40, stock: 50 },
    ],
  },
  {
    id: "p_snack_lays_magic_masala",
    name: "Lay's India's Magic Masala Potato Chips",
    brand: "Lay's",
    category: "c_snacks",
    descriptionEn: "Thin, crispy sliced potato chips tossed in classic spicy Indian masala seasoning blend.",
    imageUrl: "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "50", unitType: "g", price: 20, mrp: 20, stock: 75 },
      { unitVal: "115", unitType: "g", price: 48, mrp: 50, stock: 40 },
    ],
  },
  {
    id: "p_snack_maggi_noodles",
    name: "Maggi 2-Minute Masala Instant Noodles",
    brand: "Nestle Maggi",
    category: "c_snacks",
    descriptionEn: "India's favorite 2-minute instant noodles with the signature Tastemaker masala blend containing 10 roasted spices.",
    imageUrl: "https://images.unsplash.com/photo-1612927601601-6638404737ce?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "280", unitType: "g", price: 56, mrp: 60, stock: 60 },
      { unitVal: "560", unitType: "g", price: 108, mrp: 120, stock: 40 },
    ],
  },

  // ==========================================
  // 8. BISCUITS & BAKERY (c_biscuits)
  // ==========================================
  {
    id: "p_biscuit_dark_fantasy",
    name: "Sunfeast Dark Fantasy Choco Fills",
    brand: "Sunfeast",
    category: "c_biscuits",
    descriptionEn: "Crisp chocolate cookie crust with a luscious molten choco cream center that melts in your mouth.",
    imageUrl: "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "300", unitType: "g", price: 120, mrp: 150, stock: 40 },
      { unitVal: "75", unitType: "g", price: 35, mrp: 40, stock: 60 },
    ],
  },
  {
    id: "p_biscuit_good_day_butter",
    name: "Britannia Good Day Butter Cookies",
    brand: "Britannia",
    category: "c_biscuits",
    descriptionEn: "Rich butter-flavored crunchy cookies with the famous smile design, making every tea break joyful.",
    imageUrl: "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "200", unitType: "g", price: 38, mrp: 45, stock: 60 },
      { unitVal: "600", unitType: "g", price: 110, mrp: 130, stock: 30 },
    ],
  },
  {
    id: "p_biscuit_parle_g",
    name: "Parle-G Original Glucose Biscuits",
    brand: "Parle",
    category: "c_biscuits",
    descriptionEn: "India's trusted staple glucose biscuits loaded with wheat and milk goodness for instant daily energy.",
    imageUrl: "https://images.unsplash.com/photo-1590080875515-8a3a8dc5735e?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "250", unitType: "g", price: 25, mrp: 30, stock: 100 },
      { unitVal: "800", unitType: "g", price: 75, mrp: 90, stock: 50 },
    ],
  },
  {
    id: "p_bakery_modern_bread",
    name: "Modern 100% Whole Wheat Bread (Sandwich Loaf)",
    brand: "Modern",
    category: "c_biscuits",
    descriptionEn: "Soft, freshly baked 100% whole wheat bread rich in dietary fiber. Zero maida, perfect for healthy breakfast toast and sandwiches.",
    imageUrl: "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "400", unitType: "g", price: 48, mrp: 50, stock: 40 },
    ],
  },

  // ==========================================
  // 9. TEA, COFFEE & DRINKS (c_beverages)
  // ==========================================
  {
    id: "p_bev_red_label_tea",
    name: "Brooke Bond Red Label Strong CTC Leaf Tea",
    brand: "Red Label",
    category: "c_beverages",
    descriptionEn: "High-quality Assam and Dooars CTC blend. Delivers deep amber color, strong aroma, and wholesome taste for every morning cup.",
    imageUrl: "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 260, mrp: 295, stock: 40 },
      { unitVal: "250", unitType: "g", price: 135, mrp: 155, stock: 50 },
      { unitVal: "1", unitType: "kg", price: 510, mrp: 580, stock: 20 },
    ],
  },
  {
    id: "p_bev_bru_instant_coffee",
    name: "Bru Instant Coffee Jar",
    brand: "Bru",
    category: "c_beverages",
    descriptionEn: "Fine blend of 70% selected coffee beans and 30% chicory, roasted to perfection for authentic South Indian filter-style aroma.",
    imageUrl: "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "200", unitType: "g", price: 340, mrp: 380, stock: 30 },
      { unitVal: "100", unitType: "g", price: 175, mrp: 195, stock: 40 },
    ],
  },
  {
    id: "p_bev_thums_up",
    name: "Thums Up Charged Soft Drink Bottle",
    brand: "Thums Up",
    category: "c_beverages",
    descriptionEn: "Taste the thunder! Bold, fizzy, and strong spicy cola flavor for instant refreshment on hot afternoons.",
    imageUrl: "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "750", unitType: "ml", price: 40, mrp: 40, stock: 60 },
      { unitVal: "2.25", unitType: "L", price: 95, mrp: 100, stock: 30 },
    ],
  },
  {
    id: "p_bev_cadbury_bournvita",
    name: "Cadbury Bournvita Chocolate Health Drink",
    brand: "Cadbury",
    category: "c_beverages",
    descriptionEn: "Malted chocolate drink mix enriched with Inner Strength formula containing Vitamin D, Iron, and Calcium for growing kids.",
    imageUrl: "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 410, mrp: 460, stock: 25 },
      { unitVal: "500", unitType: "g", price: 215, mrp: 240, stock: 40 },
    ],
  },

  // ==========================================
  // 10. HOME & CLEANING (c_cleaning)
  // ==========================================
  {
    id: "p_clean_surf_excel",
    name: "Surf Excel Quick Wash Detergent Powder",
    brand: "Surf Excel",
    category: "c_cleaning",
    descriptionEn: "Advanced stain removal formula that dissolves quickly in water to remove tough grease, tea, and curry stains without harming fabric.",
    imageUrl: "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 145, mrp: 160, stock: 50 },
      { unitVal: "4", unitType: "kg", price: 540, mrp: 620, stock: 20 },
    ],
  },
  {
    id: "p_clean_vim_gel",
    name: "Vim Lemon Dishwash Gel Bottle",
    brand: "Vim",
    category: "c_cleaning",
    descriptionEn: "Power of 100 lemons. One spoon cleans an entire sink full of oily dishes without leaving white residue.",
    imageUrl: "https://images.unsplash.com/photo-1607613009820-a29f7bb81c04?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "750", unitType: "ml", price: 165, mrp: 185, stock: 40 },
      { unitVal: "250", unitType: "ml", price: 60, mrp: 68, stock: 60 },
    ],
  },
  {
    id: "p_clean_dettol_liquid",
    name: "Dettol Original Antiseptic Disinfectant Liquid",
    brand: "Dettol",
    category: "c_cleaning",
    descriptionEn: "Trusted first-aid antiseptic and multi-surface germ protection liquid for hygiene, bathing, and laundry.",
    imageUrl: "https://images.unsplash.com/photo-1584744982491-665216d95f8b?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "ml", price: 215, mrp: 240, stock: 35 },
      { unitVal: "1", unitType: "L", price: 395, mrp: 440, stock: 20 },
    ],
  },
  {
    id: "p_clean_colgate_paste",
    name: "Colgate Strong Teeth Calcium Dental Cream",
    brand: "Colgate",
    category: "c_cleaning",
    descriptionEn: "Amino Shakti formula helps add natural calcium to nourish your teeth from within for 2x stronger teeth.",
    imageUrl: "https://images.unsplash.com/photo-1559599101-f09722fb4948?w=600&h=400&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 210, mrp: 240, stock: 45 },
      { unitVal: "200", unitType: "g", price: 98, mrp: 110, stock: 60 },
    ],
  },
];

async function seedProducts() {
  console.log(`Starting to seed ${products40.length} authentic supermarket products across 10 categories...`);

  let successCount = 0;
  let errorCount = 0;

  for (const p of products40) {
    const brand = p.brand;
    const nameTe = "";
    const shortEn = p.name;
    const shortTe = "";
    const descEn = p.descriptionEn;
    const descTe = "";
    const isListed = "true";

    const descriptionFormatted = `${brand} ::: ${nameTe} ::: ${shortEn} ::: ${shortTe} ::: ${descEn} ::: ${descTe} ::: ${isListed}`;

    const appSyncVariants = p.variants.map((v, idx) => {
      const vId = `${p.id}_v${idx + 1}`;
      const sku = `SKU-${p.id.toUpperCase()}-${v.unitVal}${v.unitType.toUpperCase()}`;
      const sizeFormatted = `${v.unitVal}:::${v.unitType}:::${v.mrp}:::${sku}:::${vId}`;
      return {
        size: sizeFormatted,
        price: v.price,
        stock: v.stock,
      };
    });

    const mutation = `
      mutation PutOrUpdateProduct($input: UpdateProductInput!) {
        updateProduct(input: $input) {
          id
          name
          category
          description
          imageUrls
          variants {
            size
            price
            stock
          }
        }
      }
    `;

    const input = {
      id: p.id,
      name: p.name,
      category: p.category,
      description: descriptionFormatted,
      imageUrls: [p.imageUrl],
      variants: appSyncVariants,
    };

    const createMutation = `
      mutation CreateProduct($input: CreateProductInput!) {
        createProduct(input: $input) {
          id
          name
          category
          description
          imageUrls
          variants {
            size
            price
            stock
          }
        }
      }
    `;

    try {
      // First attempt update
      const res = await executeGraphQL(mutation, { input });
      if (res?.updateProduct?.id) {
        successCount++;
        console.log(`[${successCount}/${products40.length}] Updated '${p.name}' (${p.category})`);
      } else {
        const createRes = await executeGraphQL(createMutation, { input });
        if (createRes?.createProduct?.id) {
          successCount++;
          console.log(`[${successCount}/${products40.length}] Created '${p.name}' (${p.category})`);
        }
      }
    } catch (err: any) {
      // If item does not exist, updateProduct fails with conditional check; create it!
      try {
        const createRes = await executeGraphQL(createMutation, { input });
        if (createRes?.createProduct?.id) {
          successCount++;
          console.log(`[${successCount}/${products40.length}] Created '${p.name}' (${p.category})`);
        } else {
          errorCount++;
          console.error(`Failed to create product ${p.id}`);
        }
      } catch (createErr: any) {
        errorCount++;
        console.error(`Error creating product ${p.id}:`, createErr.message);
      }
    }
  }

  console.log("\n==========================================");
  console.log(`🎉 Seeding Complete!`);
  console.log(`✅ Successfully Synced: ${successCount} / ${products40.length} Products`);
  console.log(`❌ Errors: ${errorCount}`);
  console.log("==========================================");
}

seedProducts().catch(console.error);

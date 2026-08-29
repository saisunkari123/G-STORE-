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
  nameTe: string;
  brand: string;
  category: string;
  shortDescEn: string;
  shortDescTe: string;
  descriptionEn: string;
  descriptionTe: string;
  imageUrl: string;
  variants: ProductVariantSeed[];
}

const realProducts: ProductSeed[] = [
  // ==========================================
  // 1. RICE BAGS, GRAINS & FLOURS (12 Products)
  // ==========================================
  {
    id: "p_rice_lalitha_hmt",
    name: "Lalitha Brand HMT Premium Rice",
    nameTe: "లలిత హెచ్.ఎమ్.టి రైస్",
    brand: "Lalitha",
    category: "c_rice",
    shortDescEn: "Aged premium quality soft aromatic rice for daily family meals.",
    shortDescTe: "మృదువైన మరియు సువాసనగల ప్రీమియం నాణ్యమైన బియ్యం.",
    descriptionEn: "Lalitha Brand HMT Rice is naturally aged for 12+ months, ensuring slender, non-sticky grains that expand beautifully upon cooking. Ideal for daily lunch and biryanis.",
    descriptionTe: "లలిత బ్రాండ్ హెచ్.ఎమ్.టి బియ్యం 12 నెలలకు పైగా నిల్వ ఉంచిన నాణ్యమైన ధాన్యం. అన్నం పొడిపొడిగా మరియు మృదువుగా వస్తుంది.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "26", unitType: "kg", price: 1480, mrp: 1650, stock: 50 },
      { unitVal: "10", unitType: "kg", price: 590, mrp: 660, stock: 30 },
      { unitVal: "5", unitType: "kg", price: 300, mrp: 340, stock: 25 },
    ],
  },
  {
    id: "p_rice_sona_masoori",
    name: "Kurnool Sona Masoori Raw Rice",
    nameTe: "కర్నూలు సోనా మసూరి బియ్యం",
    brand: "Bell Brand",
    category: "c_rice",
    shortDescEn: "Old crop lightweight and easily digestible South Indian staple rice.",
    shortDescTe: "తేలికగా జీర్ణమయ్యే కర్నూలు పాత సోనా మసూరి బియ్యం.",
    descriptionEn: "Authentic Kurnool Sona Masoori raw rice, lightweight and aromatic with low starch content. Perfect for daily South Indian meals, curd rice, and pulihora.",
    descriptionTe: "స్వచ్ఛమైన కర్నూలు సోనా మసూరి బియ్యం. తక్కువ పిండి పదార్ధంతో ఆరోగ్యకరమైనది మరియు రుచికరమైనది.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "26", unitType: "kg", price: 1420, mrp: 1580, stock: 40 },
      { unitVal: "10", unitType: "kg", price: 560, mrp: 620, stock: 30 },
      { unitVal: "5", unitType: "kg", price: 290, mrp: 320, stock: 20 },
    ],
  },
  {
    id: "p_rice_bpt_sannalu",
    name: "BPT 5204 Andhra Sannalu Rice",
    nameTe: "బి.పి.టి 5204 సన్న బియ్యం",
    brand: "Sri Krishna",
    category: "c_rice",
    shortDescEn: "Fine slender grains preferred across Andhra Pradesh.",
    shortDescTe: "ఆంధ్రాలో అత్యంత ప్రాచుర్యం పొందిన సన్నని బియ్యం.",
    descriptionEn: "BPT 5204 (Samba Mahsuri) is renowned for its slender grain texture and delicious taste when paired with dal and sambar.",
    descriptionTe: "బి.పి.టి 5204 సన్న బియ్యం పప్పు మరియు సాంబారుతో ఎంతో రుచికరంగా ఉంటుంది.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "26", unitType: "kg", price: 1380, mrp: 1520, stock: 35 },
      { unitVal: "10", unitType: "kg", price: 540, mrp: 600, stock: 25 },
    ],
  },
  {
    id: "p_rice_india_gate_basmati",
    name: "India Gate Feast Rozzana Basmati Rice",
    nameTe: "ఇండియా గేట్ బాస్మతి రైస్",
    brand: "India Gate",
    category: "c_rice",
    shortDescEn: "Long grain aromatic basmati rice for daily pulao and fried rice.",
    shortDescTe: "రోజువారీ పులావ్ మరియు ఫ్రైడ్ రైస్ కోసం పొడవైన బాస్మతి బియ్యం.",
    descriptionEn: "India Gate Rozzana Basmati rice brings royal aroma and distinct long slender grains to your special weekend dinners and fried rice dishes.",
    descriptionTe: "రాయల్ సువాసన మరియు పొడవైన గింజలతో బిర్యానీలు మరియు పులావ్‌లకు అనువైనది.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "5", unitType: "kg", price: 460, mrp: 525, stock: 30 },
      { unitVal: "1", unitType: "kg", price: 98, mrp: 115, stock: 50 },
    ],
  },
  {
    id: "p_rice_daawat_super",
    name: "Daawat Super Long Grain Basmati Rice",
    nameTe: "దావత్ బాస్మతి రైస్",
    brand: "Daawat",
    category: "c_rice",
    shortDescEn: "Extra long pearly white grains aged for royal Biryanis.",
    shortDescTe: "రాయల్ బిర్యానీల కోసం ప్రత్యేకమైన పొడవైన బాస్మతి బియ్యం.",
    descriptionEn: "Daawat Super Basmati Rice grains elongate up to 2.5x after cooking without breaking, delivering unmatched aroma and texture.",
    descriptionTe: "వండినప్పుడు 2.5 రెట్లు పొడవు పెరిగే ప్రీమియం దావత్ బాస్మతి బియ్యం.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "5", unitType: "kg", price: 620, mrp: 710, stock: 20 },
      { unitVal: "1", unitType: "kg", price: 130, mrp: 150, stock: 40 },
    ],
  },
  {
    id: "p_rice_idli_boiled",
    name: "Special Idli Rice (Boiled Short Grain)",
    nameTe: "స్పెషల్ ఇడ్లీ బియ్యం",
    brand: "G-Store Select",
    category: "c_rice",
    shortDescEn: "Plump parboiled rice for soft, fluffy South Indian idlis.",
    shortDescTe: "మల్లెపూవు లాంటి మెత్తని ఇడ్లీల కోసం ప్రత్యేక బియ్యం.",
    descriptionEn: "Carefully selected short grain parboiled rice that ferments efficiently with urad dal to make soft, sponge-like idlis and crispy dosas.",
    descriptionTe: "మినపప్పుతో సులభంగా రుబ్బి మెత్తని ఇడ్లీలు తయారుచేసుకోవడానికి అనువైనది.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "26", unitType: "kg", price: 1080, mrp: 1200, stock: 30 },
      { unitVal: "10", unitType: "kg", price: 430, mrp: 480, stock: 30 },
      { unitVal: "5", unitType: "kg", price: 220, mrp: 250, stock: 25 },
    ],
  },
  {
    id: "p_flour_aashirvaad_atta",
    name: "Aashirvaad Superior MP Sharbati Atta",
    nameTe: "ఆశీర్వాద్ గోధుమ పిండి",
    brand: "Aashirvaad",
    category: "c_rice",
    shortDescEn: "100% whole wheat chakki atta for extra soft rotis and puris.",
    shortDescTe: "మెత్తని చపాతీలు మరియు పూరీల కోసం 100% స్వచ్ఛమైన గోధుమ పిండి.",
    descriptionEn: "Aashirvaad Atta is made from heavy golden Sharbati wheat grains from Madhya Pradesh, keeping rotis soft for hours with zero added maida.",
    descriptionTe: "మధ్యప్రదేశ్ శర్బతి గోధుమలతో తయారైన పిండి. చపాతీలు చాలా సేపు మెత్తగా ఉంటాయి.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "10", unitType: "kg", price: 485, mrp: 540, stock: 40 },
      { unitVal: "5", unitType: "kg", price: 250, mrp: 280, stock: 50 },
      { unitVal: "1", unitType: "kg", price: 54, mrp: 60, stock: 60 },
    ],
  },
  {
    id: "p_flour_pillsbury_atta",
    name: "Pillsbury Chakki Fresh Whole Wheat Atta",
    nameTe: "పిల్స్‌బరీ గోధుమ పిండి",
    brand: "Pillsbury",
    category: "c_rice",
    shortDescEn: "Stone-ground chakki whole wheat flour rich in natural dietary fiber.",
    shortDescTe: "సహజ పీచు పదార్ధాలతో కూడిన సంప్రదాయ చక్కి గోధుమ పిండి.",
    descriptionEn: "Pillsbury Chakki Fresh Atta locks in natural dietary fiber and essential wheat nutrients for healthy daily rotis.",
    descriptionTe: "సహజ పోషకాలు మరియు పీచు పదార్ధాలతో కూడిన పిల్స్‌బరీ పిండి.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "10", unitType: "kg", price: 470, mrp: 520, stock: 25 },
      { unitVal: "5", unitType: "kg", price: 245, mrp: 270, stock: 35 },
    ],
  },
  {
    id: "p_grain_bombay_rava",
    name: "Naga Roasted Bombay Rava (Upma Sooji)",
    nameTe: "నాగ బొంబాయి రవ్వ",
    brand: "Naga",
    category: "c_rice",
    shortDescEn: "Pre-roasted fine semolina sooji for quick lump-free breakfast upma.",
    shortDescTe: "ఉప్మా మరియు కేసరి కోసం వేయించిన స్వచ్ఛమైన బొంబాయి రవ్వ.",
    descriptionEn: "Made from premium durum wheat semolina, pre-roasted to deliver golden, lump-free breakfast upma, halwa, and rava kesari.",
    descriptionTe: "రుచికరమైన ఉప్మా మరియు హల్వా తయారీకి ఎంతో అనువైన నాణ్యమైన రవ్వ.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 46, mrp: 52, stock: 50 },
      { unitVal: "500", unitType: "g", price: 24, mrp: 28, stock: 60 },
    ],
  },
  {
    id: "p_grain_idli_rava",
    name: "Sri Lalitha Idli Rava (Fine Rice Rava)",
    nameTe: "శ్రీ లలిత ఇడ్లీ రవ్వ",
    brand: "Lalitha",
    category: "c_rice",
    shortDescEn: "Evenly granulated pure rice rava for white fluffy idlis.",
    shortDescTe: "సాఫ్ట్ ఇడ్లీల కోసం సమానంగా పిండిచేసిన ఇడ్లీ రవ్వ.",
    descriptionEn: "Milled from high quality raw rice with uniform granulation. Soaks easily and yields fluffy, light South Indian idlis.",
    descriptionTe: "మల్లెపూవు లాంటి మెత్తని ఇడ్లీల తయారీకి ప్రత్యేకమైన లలిత ఇడ్లీ రవ్వ.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 42, mrp: 48, stock: 60 },
      { unitVal: "500", unitType: "g", price: 22, mrp: 25, stock: 70 },
    ],
  },
  {
    id: "p_rice_brown_diet",
    name: "Organic Brown Rice (Single Polish Diet Rice)",
    nameTe: "ఆర్గానిక్ బ్రౌన్ రైస్",
    brand: "24 Mantra",
    category: "c_rice",
    shortDescEn: "Unpolished whole grain brown rice high in fiber and low glycemic index.",
    shortDescTe: "మధుమేహం మరియు ఫిట్‌నెస్ కోసం అధిక ఫైబర్ బ్రౌన్ రైస్.",
    descriptionEn: "Retains the outer bran layer with essential B vitamins, minerals, and dietary fiber. Recommended for diabetes management and weight loss.",
    descriptionTe: "పోషకాలు మరియు విటమిన్లు నిండిన ఆరోగ్యకరమైన బ్రౌన్ రైస్.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "5", unitType: "kg", price: 390, mrp: 440, stock: 20 },
      { unitVal: "1", unitType: "kg", price: 82, mrp: 95, stock: 30 },
    ],
  },
  {
    id: "p_flour_besan_fortune",
    name: "Fortune Besan (Gram Flour for Pakodas)",
    nameTe: "ఫార్చూన్ శనగపిండి",
    brand: "Fortune",
    category: "c_rice",
    shortDescEn: "100% chana dal milled fine besan for crispy snacks and sweets.",
    shortDescTe: "పకోడీలు, బజ్జీలు మరియు మిఠాయిల కోసం స్వచ్ఛమైన శనగపిండి.",
    descriptionEn: "Milled from 100% pure desi chana dal with zero mixing. Perfect for crispy hot evening pakodas, mirchi bajjis, and besan ladoos.",
    descriptionTe: "100% స్వచ్ఛమైన శనగపప్పుతో తయారైన ఫార్చూన్ శనగపిండి.",
    imageUrl: "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 95, mrp: 110, stock: 40 },
      { unitVal: "500", unitType: "g", price: 50, mrp: 58, stock: 50 },
    ],
  },

  // ==========================================
  // 2. COOKING OILS & GHEE (10 Products)
  // ==========================================
  {
    id: "p_oil_freedom_sunflower",
    name: "Freedom Refined Sunflower Oil Pouch",
    nameTe: "ఫ్రీడమ్ సన్‌ఫ్లవర్ ఆయిల్",
    brand: "Freedom",
    category: "c_oil",
    shortDescEn: "Heart-friendly refined sunflower oil enriched with Vitamins A & D.",
    shortDescTe: "విటమిన్లు ఎ మరియు డి కలిగిన తేలికపాటి సన్‌ఫ్లవర్ నూనె.",
    descriptionEn: "Freedom Sunflower Oil is non-greasy and light on the stomach with high smoke point for deep frying and daily Andhra curries.",
    descriptionTe: "ఆరోగ్యకరమైన మరియు తేలికపాటి ఫ్రీడమ్ సన్‌ఫ్లవర్ నూనె.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 118, mrp: 135, stock: 80 },
      { unitVal: "5", unitType: "L", price: 585, mrp: 670, stock: 25 },
    ],
  },
  {
    id: "p_oil_fortune_sunlite",
    name: "Fortune Sunlite Sunflower Oil",
    nameTe: "ఫార్చూన్ సన్‌ఫ్లవర్ ఆయిల్",
    brand: "Fortune",
    category: "c_oil",
    shortDescEn: "Light, healthy, and easy to digest refined cooking sunflower oil.",
    shortDescTe: "రోజువారీ వంటల కోసం తేలికైన ఫార్చూన్ సన్‌ఫ్లవర్ నూనె.",
    descriptionEn: "Fortune Sunlite contains natural Vitamin E antioxidants and fortifies your body's immune system while keeping curries light and flavorful.",
    descriptionTe: "విటమిన్ ఇ మరియు యాంటీ ఆక్సిడెంట్లతో కూడిన నాణ్యమైన నూనె.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 120, mrp: 138, stock: 60 },
      { unitVal: "5", unitType: "L", price: 595, mrp: 680, stock: 20 },
    ],
  },
  {
    id: "p_oil_gemini_sunflower",
    name: "Gemini Pure Sunflower Oil with Nutri-V",
    nameTe: "జెమిని సన్‌ఫ్లవర్ ఆయిల్",
    brand: "Gemini",
    category: "c_oil",
    shortDescEn: "Enriched with Nutri-V for active energy and light tasty cooking.",
    shortDescTe: "న్యూట్రీ-వి పోషకాలతో కూడిన స్వచ్ఛమైన జెమిని నూనె.",
    descriptionEn: "Gemini Sunflower Oil comes with Nutri-V complex keeping food light and crispy without soaking excess oil.",
    descriptionTe: "తక్కువ నూనె పీల్చుకునే గుణం కలిగిన నాణ్యమైన నూనె.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 116, mrp: 132, stock: 50 },
      { unitVal: "5", unitType: "L", price: 575, mrp: 660, stock: 15 },
    ],
  },
  {
    id: "p_oil_gold_drop_groundnut",
    name: "Gold Drop Filtered Groundnut Oil (Verusenaga Nune)",
    nameTe: "గోల్డ్ డ్రాప్ వేరుశనగ నూనె",
    brand: "Gold Drop",
    category: "c_oil",
    shortDescEn: "Traditional nutty aroma for authentic Andhra pickles, fries, and curries.",
    shortDescTe: "ఆవకాయ పచ్చళ్ళు మరియు వేపుళ్ళ కోసం ఘుమఘుమలాడే వేరుశనగ నూనె.",
    descriptionEn: "Extracted from premium selected groundnut seeds. Essential for authentic Andhra avakaya pickles and deep fried snacks.",
    descriptionTe: "స్వచ్ఛమైన వేరుశనగ గింజల నుండి తీసిన సంప్రదాయ వంట నూనె.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 168, mrp: 190, stock: 40 },
      { unitVal: "5", unitType: "L", price: 825, mrp: 940, stock: 15 },
    ],
  },
  {
    id: "p_oil_fortune_mustard",
    name: "Fortune Kachi Ghani Mustard Oil",
    nameTe: "ఫార్చూన్ ఆవనూనె",
    brand: "Fortune",
    category: "c_oil",
    shortDescEn: "Cold-pressed pungent mustard oil with natural antioxidants.",
    shortDescTe: "గాఢమైన సువాసనతో కూడిన స్వచ్ఛమైన ఆవనూనె.",
    descriptionEn: "Naturally cold-pressed (Kachi Ghani) to preserve distinct pungency and strong aroma for gravies and fish preparations.",
    descriptionTe: "సంప్రదాయ పద్ధతిలో తీసిన గాఢమైన ఆవనూనె.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 142, mrp: 160, stock: 30 },
      { unitVal: "500", unitType: "ml", price: 74, mrp: 85, stock: 40 },
    ],
  },
  {
    id: "p_oil_idhayam_sesame",
    name: "Idhayam Traditional Sesame / Gingelly Oil",
    nameTe: "ఇదయం నువ్వుల నూనె",
    brand: "Idhayam",
    category: "c_oil",
    shortDescEn: "Pure cold-pressed sesame oil with authentic South Indian flavor.",
    shortDescTe: "సంప్రదాయ నువ్వుల నూనె - పచ్చళ్ళు మరియు పొడులకు ఎంతో రుచి.",
    descriptionEn: "Idhayam sesame oil is made from hand-picked white sesame seeds with palm jaggery. Perfect for podi idli and tamarind rice.",
    descriptionTe: "స్వచ్ఛమైన నువ్వుల నుండి తయారైన ఆరోగ్యకరమైన నూనె.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 340, mrp: 380, stock: 25 },
      { unitVal: "500", unitType: "ml", price: 175, mrp: 195, stock: 30 },
    ],
  },
  {
    id: "p_oil_parachute_coconut",
    name: "Parachute 100% Pure Coconut Cooking Oil",
    nameTe: "ప్యారాచూట్ కొబ్బరి నూనె",
    brand: "Parachute",
    category: "c_oil",
    shortDescEn: "100% pure edible coconut oil made from sun-dried coconuts.",
    shortDescTe: "స్వచ్ఛమైన కొబ్బరి నూనె - వంటలకు మరియు ఆరోగ్యానికి శ్రేష్టం.",
    descriptionEn: "Extracted from naturally sun-dried copras with no preservatives or added chemicals.",
    descriptionTe: "సహజమైన కొబ్బరి నుండి తీసిన 100% స్వచ్ఛమైన నూనె.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 260, mrp: 295, stock: 30 },
      { unitVal: "500", unitType: "ml", price: 135, mrp: 155, stock: 40 },
    ],
  },
  {
    id: "p_ghee_grb_cow",
    name: "GRB Pure Cow Ghee Aroma Protected",
    nameTe: "జి.ఆర్.బి ఆవు నెయ్యి",
    brand: "GRB",
    category: "c_oil",
    shortDescEn: "Granular texture and royal aroma packed in aroma-locked containers.",
    shortDescTe: "పూస పూసగా ఉండే స్వచ్ఛమైన సువాసనగల ఆవు నెయ్యి.",
    descriptionEn: "GRB Cow Ghee is made using traditional bilona churning methods to deliver granular texture and rich aroma to sweets and hot rice.",
    descriptionTe: "వేడి వేడి అన్నంలో పప్పుతో కలిపి తింటే అమృతతుల్యమైన రుచినిచ్చే నెయ్యి.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 680, mrp: 750, stock: 20 },
      { unitVal: "500", unitType: "ml", price: 350, mrp: 390, stock: 35 },
      { unitVal: "200", unitType: "ml", price: 148, mrp: 165, stock: 50 },
    ],
  },
  {
    id: "p_ghee_durga_pure",
    name: "Durga Pure Ghee (Traditional Andhra Taste)",
    nameTe: "దుర్గా స్వచ్ఛమైన నెయ్యి",
    brand: "Durga",
    category: "c_oil",
    shortDescEn: "Authentic coastal Andhra style granular cow ghee.",
    shortDescTe: "ఆంధ్రా సంప్రదాయ రుచితో కూడిన స్వచ్ఛమైన నెయ్యి.",
    descriptionEn: "Durga Ghee is a household favorite in North Andhra, renowned for its rich golden color, aroma, and rich flavor in sweets.",
    descriptionTe: "ఉత్తరాంధ్ర ప్రజల అభిమాన దుర్గా నెయ్యి.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 640, mrp: 710, stock: 25 },
      { unitVal: "500", unitType: "ml", price: 330, mrp: 365, stock: 40 },
    ],
  },
  {
    id: "p_oil_dalda_vanaspati",
    name: "Dalda Vanaspati Pouch",
    nameTe: "డాల్డా వనస్పతి",
    brand: "Dalda",
    category: "c_oil",
    shortDescEn: "Hydrogenated vegetable fat for crispy bhaturas, puris, and sweets.",
    shortDescTe: "పూరీలు మరియు మిఠాయిల కోసం నాణ్యమైన డాల్డా.",
    descriptionEn: "Dalda Vanaspati provides ideal texture and crispiness to bakery items, samosas, and Indian festive sweets.",
    descriptionTe: "సమోసాలు మరియు మిఠాయిల తయారీకి ప్రత్యేకమైన డాల్డా.",
    imageUrl: "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 110, mrp: 125, stock: 30 },
      { unitVal: "500", unitType: "ml", price: 58, mrp: 68, stock: 40 },
    ],
  },

  // ==========================================
  // 3. DALS, PULSES, SUGAR & SALT (10 Products)
  // ==========================================
  {
    id: "p_dal_toor_tata",
    name: "Tata Sampann Unpolished Toor Dal (Kandi Pappu)",
    nameTe: "టాటా కందిపప్పు",
    brand: "Tata Sampann",
    category: "c_dal",
    shortDescEn: "100% unpolished toor dal rich in plant proteins with zero water polish.",
    shortDescTe: "నీటి పాలిష్ లేని స్వచ్ఛమైన ప్రొటీన్ కందిపప్పు.",
    descriptionEn: "Tata Sampann Toor Dal retains natural nutrients and cooks faster into thick, creamy sambar and mudda pappu.",
    descriptionTe: "ముద్దపప్పు మరియు సాంబారు కోసం నాణ్యమైన టాటా కందిపప్పు.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 165, mrp: 185, stock: 50 },
      { unitVal: "500", unitType: "g", price: 85, mrp: 96, stock: 60 },
    ],
  },
  {
    id: "p_dal_toor_fatka",
    name: "G-Store Premium Gold Toor Dal (Fatka Kandi Pappu)",
    nameTe: "ప్రీమియం ఫట్కా కందిపప్పు",
    brand: "G-Store Select",
    category: "c_dal",
    shortDescEn: "Golden yellow fatka toor dal that dissolves smoothly into thick dal.",
    shortDescTe: "ఘుమఘుమలాడే పప్పు చారు కోసం ప్రీమియం ఫట్కా కందిపప్పు.",
    descriptionEn: "Specially sourced fatka toor dal with zero stones or debris. Delivers vibrant yellow color and authentic home taste.",
    descriptionTe: "రాళ్ళు నలకలు లేని శుభ్రమైన నాణ్యమైన కందిపప్పు.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 155, mrp: 175, stock: 60 },
      { unitVal: "500", unitType: "g", price: 80, mrp: 90, stock: 80 },
    ],
  },
  {
    id: "p_dal_moong_tata",
    name: "Tata Sampann Yellow Moong Dal (Pesara Pappu)",
    nameTe: "పెసరపప్పు",
    brand: "Tata Sampann",
    category: "c_dal",
    shortDescEn: "Light, easily digestible yellow split lentils for khichdi and dal fry.",
    shortDescTe: "తేలికగా జీర్ణమయ్యే పసుపు పెసరపప్పు - కిచిడీ మరియు పప్పులకు శ్రేష్టం.",
    descriptionEn: "Unpolished split moong dal with no artificial luster. Cooks in minutes and provides wholesome protein.",
    descriptionTe: "ప్రొటీన్లతో కూడిన తేలికపాటి పెసరపప్పు.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 128, mrp: 145, stock: 45 },
      { unitVal: "500", unitType: "g", price: 66, mrp: 75, stock: 50 },
    ],
  },
  {
    id: "p_dal_urad_gota",
    name: "Andhra Polished Urad Gota (Minapa Gundlu for Dosa)",
    nameTe: "గుండు మినపప్పు (మినప గుండ్లు)",
    brand: "Krishna Brand",
    category: "c_dal",
    shortDescEn: "Whole white urad gota that fluffs into huge volume for idli/dosa batter.",
    shortDescTe: "ఇడ్లీ మరియు దోశెల కోసం అధిక పిండినిచ్చే మినప గుండ్లు.",
    descriptionEn: "Premium unhusked whole urad dal known for high batter yield and maximum fluffiness in idlis, vadas, and dosas.",
    descriptionTe: "గారెలు, ఇడ్లీలు మరియు దోశెల కోసం ప్రత్యేకంగా ఎంపిక చేసిన మినప గుండ్లు.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 138, mrp: 155, stock: 50 },
      { unitVal: "500", unitType: "g", price: 72, mrp: 82, stock: 60 },
    ],
  },
  {
    id: "p_dal_urad_split",
    name: "Unpolished Urad Dal Split (Chaya Minapappu)",
    nameTe: "పొట్టు మినపప్పు",
    brand: "G-Store Select",
    category: "c_dal",
    shortDescEn: "Split urad dal with natural black skin for authentic vada and sunnundalu.",
    shortDescTe: "సున్నుండలు మరియు వడల కోసం సంప్రదాయ పొట్టు మినపప్పు.",
    descriptionEn: "Rich in fiber and authentic earthy flavor, ideal for traditional Andhra sunnundalu and medu vadas.",
    descriptionTe: "ఆంధ్రా సంప్రదాయ సున్నుండల తయారీకి ప్రత్యేకమైన పొట్టు మినపప్పు.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 125, mrp: 140, stock: 40 },
      { unitVal: "500", unitType: "g", price: 65, mrp: 74, stock: 40 },
    ],
  },
  {
    id: "p_dal_chana_desi",
    name: "Desi Chana Dal (Senaga Pappu for Tadka & Sweets)",
    nameTe: "శనగపప్పు",
    brand: "G-Store Select",
    category: "c_dal",
    shortDescEn: "Nutritious golden chana dal for daily tadka, chutneys, and puran poli.",
    shortDescTe: "తాలింపులు, చట్నీలు మరియు బొబ్బట్ల కోసం స్వచ్ఛమైన శనగపప్పు.",
    descriptionEn: "Crisp and flavorful desi chana dal for south Indian coconut chutneys, vegetable poriyals, and festive bobbatlu.",
    descriptionTe: "కొబ్బరి చట్నీలు మరియు బొబ్బట్ల కోసం నాణ్యమైన శనగపప్పు.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 92, mrp: 105, stock: 50 },
      { unitVal: "500", unitType: "g", price: 48, mrp: 56, stock: 60 },
    ],
  },
  {
    id: "p_pulse_kabuli_chana",
    name: "Big Kabuli Chana (White Chickpeas for Chole)",
    nameTe: "కాబూలీ శనగలు (తెల్ల శనగలు)",
    brand: "G-Store Select",
    category: "c_dal",
    shortDescEn: "Large size white chickpeas for Punjabi chole bhature and salads.",
    shortDescTe: "రుచికరమైన చోలే బటూరే కోసం పెద్ద కాబూలీ శనగలు.",
    descriptionEn: "Jumbo sized white chickpeas that expand evenly and become buttery soft when boiled.",
    descriptionTe: "ఉడికించినప్పుడు మెత్తగా అయ్యే పెద్ద కాబూలీ శనగలు.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 145, mrp: 165, stock: 35 },
      { unitVal: "500", unitType: "g", price: 76, mrp: 88, stock: 45 },
    ],
  },
  {
    id: "p_pulse_kala_chana",
    name: "Brown Desi Kala Chana (Black Chickpeas)",
    nameTe: "నల్ల శనగలు",
    brand: "G-Store Select",
    category: "c_dal",
    shortDescEn: "Iron and protein rich brown chickpeas for guggillu and healthy snacks.",
    shortDescTe: "గుగ్గిళ్ళు మరియు ప్రసాదాల కోసం ఐరన్ నిండిన నల్ల శనగలు.",
    descriptionEn: "High protein desi black chickpeas for morning sprouts, temple prasadam guggillu, and spicy Andhra curries.",
    descriptionTe: "మొలకలు మరియు గుగ్గిళ్ళ తయారీకి బలవర్ధకమైన ఆహారం.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 88, mrp: 100, stock: 40 },
      { unitVal: "500", unitType: "g", price: 46, mrp: 54, stock: 50 },
    ],
  },
  {
    id: "p_staple_madhur_sugar",
    name: "Madhur Pure & Hygienic Sulphurless Sugar",
    nameTe: "స్వచ్ఛమైన పంచదార",
    brand: "Madhur",
    category: "c_dal",
    shortDescEn: "100% sulphur-free sparkling white sugar crystals for tea, coffee, and sweets.",
    shortDescTe: "సల్ఫర్ లేని స్వచ్ఛమైన తెల్లటి పంచదార.",
    descriptionEn: "Refined without harmful sulphur chemicals. Dissolves crystal clear into beverages, payasam, and festive sweets.",
    descriptionTe: "టీ, కాఫీ మరియు పాయసాల కోసం స్వచ్ఛమైన పంచదార.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "5", unitType: "kg", price: 235, mrp: 265, stock: 40 },
      { unitVal: "1", unitType: "kg", price: 48, mrp: 55, stock: 80 },
    ],
  },
  {
    id: "p_staple_tata_salt",
    name: "Tata Salt Vacuum Evaporated Iodised Salt",
    nameTe: "టాటా అయోడైజ్డ్ ఉప్పు",
    brand: "Tata Salt",
    category: "c_dal",
    shortDescEn: "India's #1 vacuum evaporated iodised salt for daily healthy cooking.",
    shortDescTe: "దేశం నమ్మిన స్వచ్ఛమైన అయోడైజ్డ్ టాటా ఉప్పు.",
    descriptionEn: "Tata Salt contains the right amount of iodine required for mental development and physical well-being.",
    descriptionTe: "రోజువారీ వంటల కోసం స్వచ్ఛమైన అయోడైజ్డ్ ఉప్పు.",
    imageUrl: "https://images.unsplash.com/photo-1585994192701-f1a505c8574a?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 27, mrp: 30, stock: 120 },
    ],
  },

  // ==========================================
  // 4. DAIRY & BREAKFAST ESSENTIALS (10 Products)
  // ==========================================
  {
    id: "p_dairy_heritage_special_milk",
    name: "Heritage Special Full Cream Milk (Orange Packet)",
    nameTe: "హెరిటేజ్ ఫుల్ క్రీమ్ పాలు",
    brand: "Heritage",
    category: "c_dairy",
    shortDescEn: "6.0% Fat rich milk for thick curds, creamy tea, and sweet making.",
    shortDescTe: "చిక్కటి పెరుగు మరియు రుచికరమైన టీ కోసం ఫుల్ క్రీమ్ పాలు.",
    descriptionEn: "Heritage Special milk delivers rich taste and creamy texture with 6.0% Fat and 9.0% SNF. Perfect for thick curd and traditional kheer.",
    descriptionTe: "చిక్కటి వెన్న మరియు పెరుగు కోసం హెరిటేజ్ ఆరెంజ్ ప్యాకెట్ పాలు.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "ml", price: 37, mrp: 38, stock: 60 },
      { unitVal: "1", unitType: "L", price: 73, mrp: 75, stock: 40 },
    ],
  },
  {
    id: "p_dairy_heritage_toned_milk",
    name: "Heritage Daily Health Toned Milk (Blue Packet)",
    nameTe: "హెరిటేజ్ టోన్డ్ పాలు",
    brand: "Heritage",
    category: "c_dairy",
    shortDescEn: "3.0% Fat balanced milk for daily family nutrition and coffee.",
    shortDescTe: "రోజువారీ కుటుంబ ఆరోగ్యం కోసం బ్లూ ప్యాకెట్ టోన్డ్ పాలు.",
    descriptionEn: "Pasteurized, homogenized toned milk with balanced fat content for daily tea, coffee, and growing children.",
    descriptionTe: "తేలికపాటి మరియు ఆరోగ్యకరమైన టోన్డ్ పాలు.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "ml", price: 30, mrp: 31, stock: 80 },
      { unitVal: "1", unitType: "L", price: 59, mrp: 60, stock: 50 },
    ],
  },
  {
    id: "p_dairy_amul_taaza",
    name: "Amul Taaza Homogenised Toned Milk",
    nameTe: "అమూల్ తాజా పాలు (టెట్రా ప్యాక్)",
    brand: "Amul",
    category: "c_dairy",
    shortDescEn: "UHT treated long shelf life milk with zero preservatives.",
    shortDescTe: "ఎక్కువ రోజులు నిల్వ ఉండే అమూల్ తాజా పాలు.",
    descriptionEn: "Amul Taaza requires no boiling and stays fresh in aseptic tetra packaging. Ideal for emergencies and office use.",
    descriptionTe: "మరగబెట్టాల్సిన అవసరం లేని స్వచ్ఛమైన అమూల్ పాలు.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "L", price: 74, mrp: 78, stock: 40 },
      { unitVal: "500", unitType: "ml", price: 38, mrp: 40, stock: 50 },
    ],
  },
  {
    id: "p_dairy_heritage_curd",
    name: "Heritage Fresh Thick Curd (Perugu Tub / Pouch)",
    nameTe: "హెరిటేజ్ గడ్డ పెరుగు",
    brand: "Heritage",
    category: "c_dairy",
    shortDescEn: "Thick, creamy, and mildly sweet pasteurized curd.",
    shortDescTe: "రుచికరమైన చిక్కటి హెరిటేజ్ గడ్డ పెరుగు.",
    descriptionEn: "Prepared from pasteurized standardized milk with active lactic cultures. Perfectly thick for curd rice, raita, and lassi.",
    descriptionTe: "పెరుగన్నం మరియు లస్సీల కోసం అమృతతుల్యమైన పెరుగు.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "1", unitType: "kg", price: 85, mrp: 90, stock: 30 },
      { unitVal: "500", unitType: "g", price: 38, mrp: 40, stock: 50 },
      { unitVal: "200", unitType: "g", price: 18, mrp: 20, stock: 60 },
    ],
  },
  {
    id: "p_dairy_amul_paneer",
    name: "Amul Malai Fresh Paneer (Soft Block)",
    nameTe: "అమూల్ మలై పన్నీర్",
    brand: "Amul",
    category: "c_dairy",
    shortDescEn: "Soft, spongy, protein-rich cottage cheese for curries and tikka.",
    shortDescTe: "పాలక్ పన్నీర్ మరియు కూరల కోసం మెత్తని అమూల్ పన్నీర్.",
    descriptionEn: "Made from fresh cow and buffalo milk, staying soft without breaking when cooked in paneer butter masala or grilled.",
    descriptionTe: "ప్రొటీన్ నిండిన మెత్తని స్వచ్ఛమైన అమూల్ పన్నీర్.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 230, mrp: 250, stock: 25 },
      { unitVal: "200", unitType: "g", price: 95, mrp: 105, stock: 50 },
    ],
  },
  {
    id: "p_dairy_amul_butter",
    name: "Amul Pasteurized Salted Butter",
    nameTe: "అమూల్ వెన్న",
    brand: "Amul",
    category: "c_dairy",
    shortDescEn: "Utterly butterly delicious creamy salted butter for bread, dosas, and baking.",
    shortDescTe: "దోశలు, బ్రెడ్ మరియు వంటల కోసం రుచికరమైన అమూల్ వెన్న.",
    descriptionEn: "India's iconic Amul Butter made from fresh cream. Enhances flavor in pav bhaji, dal makhani, and breakfast toast.",
    descriptionTe: "దేశమంతా మెచ్చే అమూల్ స్వచ్ఛమైన వెన్న.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 275, mrp: 295, stock: 25 },
      { unitVal: "100", unitType: "g", price: 58, mrp: 62, stock: 60 },
    ],
  },
  {
    id: "p_dairy_milky_mist_paneer",
    name: "Milky Mist Fresh Paneer",
    nameTe: "మిల్కీ మిస్ట్ పన్నీర్",
    brand: "Milky Mist",
    category: "c_dairy",
    shortDescEn: "Vacuum packed fresh paneer with soft uniform texture.",
    shortDescTe: "వ్యాక్యూమ్ ప్యాక్ చేయబడిన నాణ్యమైన పన్నీర్.",
    descriptionEn: "Milky Mist Paneer is made without artificial preservatives, offering supreme softness for gravies and snacks.",
    descriptionTe: "రుచికరమైన వంటల కోసం స్వచ్ఛమైన పన్నీర్.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "200", unitType: "g", price: 105, mrp: 118, stock: 40 },
    ],
  },
  {
    id: "p_bakery_modern_bread",
    name: "Modern Classic White Sandwich Bread",
    nameTe: "మోడరన్ బ్రెడ్",
    brand: "Modern",
    category: "c_dairy",
    shortDescEn: "Fresh soft sliced sandwich bread enriched with essential vitamins.",
    shortDescTe: "ఉదయం బ్రేక్‌ఫాస్ట్ శాండ్‌విచ్ కోసం తాజా బ్రెడ్.",
    descriptionEn: "Baked fresh daily with soft crust, ideal for cheese toast, butter jam, and vegetable sandwiches.",
    descriptionTe: "ప్రతిరోజూ తాజా బేకింగ్‌తో తయారైన మెత్తని బ్రెడ్.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "400", unitType: "g", price: 45, mrp: 50, stock: 35 },
      { unitVal: "200", unitType: "g", price: 25, mrp: 28, stock: 40 },
    ],
  },
  {
    id: "p_bakery_britannia_brown_bread",
    name: "Britannia 100% Whole Wheat Brown Bread",
    nameTe: "బ్రిటానియా బ్రౌన్ బ్రెడ్",
    brand: "Britannia",
    category: "c_dairy",
    shortDescEn: "Fiber rich 100% whole wheat brown bread for health conscious breakfast.",
    shortDescTe: "ఆరోగ్యకరమైన ఫైబర్ నిండిన హోల్ వీట్ బ్రౌన్ బ్రెడ్.",
    descriptionEn: "Britannia 100% Whole Wheat Bread is packed with dietary fiber and essential grains for an energized morning.",
    descriptionTe: "డైట్ మరియు ఫిట్‌నెస్ కోసం హోల్ వీట్ బ్రెడ్.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "400", unitType: "g", price: 55, mrp: 60, stock: 30 },
    ],
  },
  {
    id: "p_egg_farm_fresh",
    name: "Farm Fresh White Table Eggs (Graded Clean Tray)",
    nameTe: "తాజా కోడిగుడ్లు",
    brand: "Farm Select",
    category: "c_dairy",
    shortDescEn: "Hygienically cleaned and candled protein-rich fresh farm eggs.",
    shortDescTe: "శుభ్రమైన మరియు తాజా పోషకాలు నిండిన కోడిగుడ్లు.",
    descriptionEn: "Direct from local poultry farms, graded for freshness and high protein. Perfect for omelettes, boiled eggs, and egg curry.",
    descriptionTe: "రోజువారీ ప్రొటీన్ అవసరాల కోసం తాజా కోడిగుడ్లు.",
    imageUrl: "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "30", unitType: "units", price: 185, mrp: 210, stock: 30 },
      { unitVal: "6", unitType: "units", price: 42, mrp: 48, stock: 50 },
    ],
  },

  // ==========================================
  // 5. SPICES, MASALAS & DRY FRUITS (10 Products)
  // ==========================================
  {
    id: "p_spice_chilli_powder",
    name: "Aashirvaad Special Guntur Chilli Powder (Karam Podi)",
    nameTe: "గుంటూరు కారం పొడి",
    brand: "Aashirvaad",
    category: "c_spices",
    shortDescEn: "Fiery red Guntur chillies ground to perfection for authentic spicy curries.",
    shortDescTe: "ఘాటైన రంగు మరియు రుచికరమైన గుంటూరు కారం పొడి.",
    descriptionEn: "Made from sun-dried Guntur chillies, providing rich red color and sharp pungency with no artificial colors or additives.",
    descriptionTe: "ఆంధ్రా నాటు రుచినిచ్చే స్వచ్ఛమైన గుంటూరు కారం.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 185, mrp: 210, stock: 40 },
      { unitVal: "200", unitType: "g", price: 78, mrp: 90, stock: 50 },
      { unitVal: "100", unitType: "g", price: 40, mrp: 48, stock: 60 },
    ],
  },
  {
    id: "p_spice_turmeric_powder",
    name: "Aashirvaad Pure Turmeric Powder (Pasupu)",
    nameTe: "పసుపు పొడి",
    brand: "Aashirvaad",
    category: "c_spices",
    shortDescEn: "Golden yellow pure turmeric with high natural curcumin content.",
    shortDescTe: "సహజ గుణాలు నిండిన స్వచ్ఛమైన పసుపు పొడి.",
    descriptionEn: "Milled from selected Salem turmeric rhizomes, delivering natural antimicrobial protection and rich yellow color.",
    descriptionTe: "ఆరోగ్యకరమైన మరియు స్వచ్ఛమైన పసుపు.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 125, mrp: 145, stock: 40 },
      { unitVal: "200", unitType: "g", price: 54, mrp: 62, stock: 50 },
      { unitVal: "100", unitType: "g", price: 28, mrp: 34, stock: 60 },
    ],
  },
  {
    id: "p_spice_coriander_powder",
    name: "Aashirvaad Coriander Powder (Dhaniyala Podi)",
    nameTe: "ధనియాల పొడి",
    brand: "Aashirvaad",
    category: "c_spices",
    shortDescEn: "Aromatic ground coriander seeds for thick, fragrant curry gravies.",
    shortDescTe: "కూరల చిక్కదనం మరియు సువాసన కోసం ధనియాల పొడి.",
    descriptionEn: "Freshly roasted green coriander seeds pulverized to impart rich aroma and body to South Indian curries and rasams.",
    descriptionTe: "ఘుమఘుమలాడే తాజా ధనియాల పొడి.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 115, mrp: 135, stock: 35 },
      { unitVal: "200", unitType: "g", price: 50, mrp: 58, stock: 45 },
    ],
  },
  {
    id: "p_spice_garam_masala",
    name: "Everest Shahi Garam Masala (Aromatic Spice Mix)",
    nameTe: "ఎవరెస్ట్ గరం మసాలా",
    brand: "Everest",
    category: "c_spices",
    shortDescEn: "Royal blend of 13 whole roasted spices for biryanis, paneer, and curries.",
    shortDescTe: "బిర్యానీలు మరియు కూరల కోసం రాజరిక సువాసన గల గరం మసాలా.",
    descriptionEn: "Everest Shahi Garam Masala blends cardamom, cinnamon, cloves, and mace for an unforgettable royal aroma in festive dishes.",
    descriptionTe: "13 రకాల సుగంధ ద్రవ్యాలతో తయారైన గరం మసాలా.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "100", unitType: "g", price: 85, mrp: 98, stock: 50 },
      { unitVal: "50", unitType: "g", price: 45, mrp: 52, stock: 60 },
    ],
  },
  {
    id: "p_spice_kitchen_king",
    name: "Everest Kitchen King All-in-One Curry Masala",
    nameTe: "కిచెన్ కింగ్ మసాలా",
    brand: "Everest",
    category: "c_spices",
    shortDescEn: "All-purpose spice seasoning that elevates any vegetable or paneer curry.",
    shortDescTe: "అన్ని రకాల వెజిటబుల్ కూరలకు సరిపోయే ఆల్-ఇన్-వన్ మసాలా.",
    descriptionEn: "Kitchen King is the king of masalas, giving restaurant-style color, aroma, and taste to everyday mixed vegetable gravies.",
    descriptionTe: "రెస్టారెంట్ స్టైల్ కూరల కోసం ప్రత్యేకమైన మసాలా.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "100", unitType: "g", price: 82, mrp: 95, stock: 40 },
    ],
  },
  {
    id: "p_spice_whole_jeera",
    name: "Tata Sampann Whole Jeera (Cumin Seeds / Jilakara)",
    nameTe: "జీలకర్ర",
    brand: "Tata Sampann",
    category: "c_spices",
    shortDescEn: "Crisp, aromatic whole cumin seeds rich in natural essential oils.",
    shortDescTe: "తాలింపులు మరియు రసాల కోసం స్వచ్ఛమైన జీలకర్ర.",
    descriptionEn: "Unpolished whole jeera with high volatile oil content. Essential for tadka, rasam, and jeera rice.",
    descriptionTe: "జీర్ణక్రియకు ఎంతో మేలు చేసే నాణ్యమైన జీలకర్ర.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "200", unitType: "g", price: 120, mrp: 140, stock: 40 },
      { unitVal: "100", unitType: "g", price: 62, mrp: 72, stock: 50 },
    ],
  },
  {
    id: "p_dryfruit_cashew_w240",
    name: "G-Store W240 Premium Whole Cashew Nuts (Kaju)",
    nameTe: "ప్రీమియం జీడిపప్పు (కాజు)",
    brand: "G-Store Select",
    category: "c_spices",
    shortDescEn: "Large white crisp whole cashews for sweets, biryanis, and snacking.",
    shortDescTe: "తీపి వంటకాలు, పాయసాలు మరియు బిర్యానీల కోసం పెద్ద జీడిపప్పు.",
    descriptionEn: "Top-grade W240 whole crunchy cashews from Palasa. Perfect for roasting with ghee, garnishing sweets, and daily brain health.",
    descriptionTe: "పలాస నుండి సేకరించిన మొదటి రకం నాణ్యమైన జీడిపప్పు.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 475, mrp: 550, stock: 25 },
      { unitVal: "250", unitType: "g", price: 245, mrp: 285, stock: 35 },
      { unitVal: "100", unitType: "g", price: 105, mrp: 120, stock: 50 },
    ],
  },
  {
    id: "p_dryfruit_almonds_california",
    name: "California Select Almonds (Badam Giri)",
    nameTe: "కాలిఫోర్నియా బాదం పప్పు",
    brand: "Royal Select",
    category: "c_spices",
    shortDescEn: "Crunchy premium California almonds loaded with Vitamin E and protein.",
    shortDescTe: "జ్ఞాపకశక్తి మరియు ఆరోగ్యం కోసం మేలైన బాదం పప్పు.",
    descriptionEn: "100% natural, sweet California almonds. Ideal for morning soaking, badam milk, and energy-packed daily snacking.",
    descriptionTe: "రాయల్ క్వాలిటీ కాలిఫోర్నియా బాదం.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 440, mrp: 510, stock: 30 },
      { unitVal: "250", unitType: "g", price: 225, mrp: 265, stock: 40 },
      { unitVal: "100", unitType: "g", price: 95, mrp: 110, stock: 50 },
    ],
  },
  {
    id: "p_dryfruit_raisins_kismis",
    name: "Indian Golden Long Raisins (Kismis / Endu Draksha)",
    nameTe: "ఎండు ద్రాక్ష (కిస్మిస్)",
    brand: "Royal Select",
    category: "c_spices",
    shortDescEn: "Naturally sweet golden seedless raisins for payasam and desserts.",
    shortDescTe: "పాయసం మరియు తీపి వంటకాల కోసం తియ్యని కిస్మిస్.",
    descriptionEn: "Seedless long golden kismis with natural sweetness. Packed with iron, antioxidants, and instant energy.",
    descriptionTe: "సహజ తీపితో కూడిన బంగారు వర్ణ కిస్మిస్.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 195, mrp: 230, stock: 30 },
      { unitVal: "250", unitType: "g", price: 105, mrp: 125, stock: 40 },
    ],
  },
  {
    id: "p_dryfruit_lion_dates",
    name: "Lion Dates (Original Arabian Dates / Kharjooram)",
    nameTe: "లయన్ ఖర్జూరం",
    brand: "Lion Dates",
    category: "c_spices",
    shortDescEn: "Soft, seeded Arabian dates packed with natural iron and calcium.",
    shortDescTe: "రక్తహీనత నివారణ మరియు శక్తి కోసం లయన్ ఖర్జూరం.",
    descriptionEn: "Lion Dates are handpicked desert dates rich in iron and dietary fiber. Boosts hemoglobin and daily vitality.",
    descriptionTe: "ప్రతిరోజూ పిల్లలు మరియు పెద్దలకు మేలైన బలవర్ధక ఆహారం.",
    imageUrl: "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 160, mrp: 180, stock: 40 },
      { unitVal: "200", unitType: "g", price: 70, mrp: 80, stock: 50 },
    ],
  },

  // ==========================================
  // 6. SNACKS, BISCUITS & BEVERAGES (12 Products)
  // ==========================================
  {
    id: "p_snack_maggi_noodles",
    name: "Maggi 2-Minute Masala Instant Noodles",
    nameTe: "మ్యాగీ మసాలా నూడుల్స్",
    brand: "Nestle Maggi",
    category: "c_snacks",
    shortDescEn: "India's favorite masala noodles made with blend of 10 roasted spices.",
    shortDescTe: "అందరికీ ఇష్టమైన 2-నిమిషాల మ్యాగీ నూడుల్స్.",
    descriptionEn: "Maggi 2-Minute Masala Noodles comes with the signature tastemaker containing coriander, cumin, aniseed, and fenugreek.",
    descriptionTe: "పిల్లలకు మరియు పెద్దలకు ఇష్టమైన రుచికరమైన మ్యాగీ.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "4", unitType: "packs", price: 54, mrp: 56, stock: 60 },
      { unitVal: "1", unitType: "pack", price: 14, mrp: 14, stock: 100 },
    ],
  },
  {
    id: "p_snack_kurkure_masala",
    name: "Kurkure Masala Munch (Crispy Namkeen)",
    nameTe: "కుర్‌కురే మసాలా మంచ్",
    brand: "Kurkure",
    category: "c_snacks",
    shortDescEn: "Crispy crunchy puffed corn namkeen with bold Indian spices.",
    shortDescTe: "సాయంత్రం టీ సమయానికి కరకరలాడే మసాలా కుర్‌కురే.",
    descriptionEn: "Tedha hai par mera hai! Kurkure Masala Munch is seasoned with zesty Indian spices for unmatched crunchiness.",
    descriptionTe: "మసాలా రుచులతో కూడిన కరకరలాడే స్నాక్.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "85", unitType: "g", price: 20, mrp: 20, stock: 80 },
      { unitVal: "40", unitType: "g", price: 10, mrp: 10, stock: 100 },
    ],
  },
  {
    id: "p_snack_lays_magic_masala",
    name: "Lay's India's Magic Masala Potato Chips",
    nameTe: "లేస్ మ్యాజిక్ మసాలా చిప్స్",
    brand: "Lay's",
    category: "c_snacks",
    shortDescEn: "Crispy sliced potato chips coated with tangy Indian spice mix.",
    shortDescTe: "రుచికరమైన బంగాళాదుంప చిప్స్.",
    descriptionEn: "Made from farm-grown fresh potatoes, thinly sliced and seasoned with aromatic Indian spices.",
    descriptionTe: "తాజా బంగాళాదుంపలతో తయారైన క్రిస్పీ లేస్ చిప్స్.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "90", unitType: "g", price: 20, mrp: 20, stock: 70 },
      { unitVal: "45", unitType: "g", price: 10, mrp: 10, stock: 100 },
    ],
  },
  {
    id: "p_snack_haldirams_bhujia",
    name: "Haldiram's Nagpur Aloo Bhujia (Crispy Sev)",
    nameTe: "హల్దీరామ్స్ ఆలూ భుజియా",
    brand: "Haldiram's",
    category: "c_snacks",
    shortDescEn: "Crispy spiced potato and gram flour sev with mint aroma.",
    shortDescTe: "రుచికరమైన కరకరలాడే ఆలూ భుజియా నమ్‌కీన్.",
    descriptionEn: "Haldiram's classic Aloo Bhujia is seasoned with fresh mint, red chilli, and amchur for the ultimate teatime snack.",
    descriptionTe: "టీ టైమ్‌లో తినడానికి అద్భుతమైన ఆలూ భుజియా.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "400", unitType: "g", price: 110, mrp: 125, stock: 40 },
      { unitVal: "150", unitType: "g", price: 46, mrp: 50, stock: 60 },
    ],
  },
  {
    id: "p_snack_haldirams_khatta_meetha",
    name: "Haldiram's Khatta Meetha Mixture",
    nameTe: "హల్దీరామ్స్ ఖట్టా మీఠా మిక్చర్",
    brand: "Haldiram's",
    category: "c_snacks",
    shortDescEn: "Sweet and tangy namkeen blend of sev, fried green peas, and sago puffs.",
    shortDescTe: "తీపి మరియు పులుపుల కలయిక గల రుచికరమైన మిక్చర్.",
    descriptionEn: "A crowd favorite combination of sweet and sour crispy ingredients that makes every bite exciting.",
    descriptionTe: "అందరికీ నచ్చే తియ్యటి మరియు పుల్లటి మిక్చర్.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "400", unitType: "g", price: 110, mrp: 125, stock: 35 },
      { unitVal: "150", unitType: "g", price: 46, mrp: 50, stock: 50 },
    ],
  },
  {
    id: "p_biscuit_good_day",
    name: "Britannia Good Day Butter Cookies",
    nameTe: "బ్రిటానియా గుడ్ డే బిస్కెట్లు",
    brand: "Britannia",
    category: "c_snacks",
    shortDescEn: "Rich buttery cookies with smiley design for cheerful tea times.",
    shortDescTe: "స్వచ్ఛమైన వెన్నతో తయారైన గుడ్ డే బిస్కెట్లు.",
    descriptionEn: "Britannia Good Day brings happiness with its rich buttery taste, crispy texture, and iconic smile patterns.",
    descriptionTe: "టీ మరియు పాలతో తినడానికి అనువైన వెన్న బిస్కెట్లు.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "200", unitType: "g", price: 38, mrp: 40, stock: 60 },
      { unitVal: "100", unitType: "g", price: 20, mrp: 20, stock: 80 },
    ],
  },
  {
    id: "p_biscuit_parle_g",
    name: "Parle-G Original Glucose Biscuits",
    nameTe: "పార్లే-జి గ్లూకోజ్ బిస్కెట్లు",
    brand: "Parle",
    category: "c_snacks",
    shortDescEn: "India's beloved glucose biscuits packed with wheat and milk nutrition.",
    shortDescTe: "శక్తినిచ్చే భారతదేశ నంబర్ 1 పార్లే-జి బిస్కెట్లు.",
    descriptionEn: "Filled with goodness of wheat and milk, Parle-G has been India's favorite tea companion for generations.",
    descriptionTe: "తరతరాలుగా అందరికీ నచ్చిన పోషక విలువల గ్లూకోజ్ బిస్కెట్లు.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "800", unitType: "g", price: 78, mrp: 85, stock: 50 },
      { unitVal: "250", unitType: "g", price: 28, mrp: 30, stock: 80 },
    ],
  },
  {
    id: "p_biscuit_dark_fantasy",
    name: "Sunfeast Dark Fantasy Choco Fills",
    nameTe: "సన్‌ఫీస్ట్ డార్క్ ఫాంటసీ కుకీస్",
    brand: "Sunfeast",
    category: "c_snacks",
    shortDescEn: "Crunchy baked chocolate cookies with molten choco creme center.",
    shortDescTe: "నోట్లో కరిగిపోయే చాక్లెట్ నిండిన డార్క్ ఫాంటసీ బిస్కెట్లు.",
    descriptionEn: "Indulge in crispy cocoa crust filled with decadent molten chocolate creme. Warm in microwave for 10 seconds for pure heaven!",
    descriptionTe: "ప్రత్యేక చాక్లెట్ రుచితో కూడిన ప్రీమియం కుకీస్.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "300", unitType: "g", price: 135, mrp: 160, stock: 35 },
      { unitVal: "75", unitType: "g", price: 38, mrp: 40, stock: 60 },
    ],
  },
  {
    id: "p_choc_dairy_milk_silk",
    name: "Cadbury Dairy Milk Silk Chocolate Bar",
    nameTe: "క్యాడ్‌బరీ డైరీ మిల్క్ సిల్క్",
    brand: "Cadbury",
    category: "c_snacks",
    shortDescEn: "Silky smooth, melt-in-mouth creamy milk chocolate bar.",
    shortDescTe: "స్మూత్ మిల్క్ చాక్లెట్ బార్.",
    descriptionEn: "Cadbury Dairy Milk Silk is crafted with richer, smoother milk chocolate that melts effortlessly on your tongue.",
    descriptionTe: "ప్రేమ మరియు సంతోషాన్ని పంచే అత్యంత రుచికరమైన సిల్క్ చాక్లెట్.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "150", unitType: "g", price: 165, mrp: 175, stock: 30 },
      { unitVal: "60", unitType: "g", price: 75, mrp: 80, stock: 50 },
    ],
  },
  {
    id: "p_bev_thums_up",
    name: "Thums Up Carbonated Soft Drink Bottle",
    nameTe: "థమ్స్ అప్ కూల్ డ్రింక్",
    brand: "Coca-Cola",
    category: "c_snacks",
    shortDescEn: "Taste the thunder! Bold, fizzy, and strong spicy cola flavor.",
    shortDescTe: "స్ట్రాంగ్ ఫిజ్ మరియు థండర్ రుచితో కూడిన థమ్స్ అప్.",
    descriptionEn: "Thums Up delivers strong fizziness and bold spice notes that perfectly complement spicy Andhra biryanis and hot fried snacks.",
    descriptionTe: "బిర్యానీలు మరియు స్నాక్స్‌తో తాగడానికి పర్ఫెక్ట్ కూల్ డ్రింక్.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "750", unitType: "ml", price: 40, mrp: 40, stock: 60 },
      { unitVal: "2.25", unitType: "L", price: 95, mrp: 100, stock: 30 },
    ],
  },
  {
    id: "p_bev_red_label_tea",
    name: "Brooke Bond Red Label Natural Care Tea",
    nameTe: "రెడ్ లేబుల్ టీ పొడి",
    brand: "Red Label",
    category: "c_snacks",
    shortDescEn: "Rich CTC leaf tea blended with 5 Ayurvedic herbs (Ashwagandha, Tulsi, Mulethi, Cardamom, Ginger).",
    shortDescTe: "తులసి, అల్లం, యాలకుల గుణాలు కలిగిన రెడ్ లేబుల్ టీ.",
    descriptionEn: "Enhances natural immunity while delivering strong taste, rich color, and refreshing aroma in every morning cup.",
    descriptionTe: "ఆరోగ్యం మరియు ఉత్తేజాన్నిచ్చే ఆయుర్వేదిక్ గుణాల టీ పొడి.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "500", unitType: "g", price: 275, mrp: 310, stock: 40 },
      { unitVal: "250", unitType: "g", price: 142, mrp: 160, stock: 50 },
    ],
  },
  {
    id: "p_bev_bru_instant_coffee",
    name: "Bru Instant Coffee Jar",
    nameTe: "బ్రూ ఇన్‌స్టంట్ కాఫీ",
    brand: "Bru",
    category: "c_snacks",
    shortDescEn: "Fine blend of 70% roasted coffee beans and 30% chicory for aromatic filter-style coffee.",
    shortDescTe: "ఘుమఘుమలాడే బ్రూ ఇన్‌స్టంట్ కాఫీ పొడి.",
    descriptionEn: "Bru Instant is made from a fine blend of the choicest plantation and robusta beans to give strong aroma and rich taste in hot milk.",
    descriptionTe: "ప్రతిరోజూ ఉదయం ఉత్సాహాన్నిచ్చే స్వచ్ఛమైన కాఫీ.",
    imageUrl: "https://images.unsplash.com/photo-1621996346565-e3d5d6281290?w=600&auto=format&fit=crop",
    variants: [
      { unitVal: "100", unitType: "g", price: 195, mrp: 215, stock: 35 },
      { unitVal: "50", unitType: "g", price: 102, mrp: 115, stock: 50 },
    ],
  },
];

async function seedLiveCatalog() {
  console.log(`\n======================================================`);
  console.log(`🚀 Seeding ${realProducts.length} Real Supermarket Products to AWS AppSync`);
  console.log(`======================================================\n`);

  let successCount = 0;
  let failCount = 0;

  for (let i = 0; i < realProducts.length; i++) {
    const p = realProducts[i];
    const progress = `[${i + 1}/${realProducts.length}]`;

    const descriptionString = `${p.brand} ::: ${p.nameTe} ::: ${p.shortDescEn} ::: ${p.shortDescTe} ::: ${p.descriptionEn} ::: ${p.descriptionTe} ::: true`;

    const formattedVariants = p.variants.map((v) => {
      const slug = p.name.toLowerCase().replace(/[^a-z0-9]+/g, "_").slice(0, 10);
      const sku = `SKU-${p.category.toUpperCase().replace("C_", "")}-${slug.toUpperCase()}-${v.unitVal}${v.unitType.toUpperCase()}`;
      const variantId = `v_${p.id}_${v.unitVal}${v.unitType}`;
      const sizeString = `${v.unitVal}:::${v.unitType}:::${v.mrp.toFixed(1)}:::${sku}:::${variantId}`;

      return {
        size: sizeString,
        price: v.price,
        stock: v.stock,
      };
    });

    const createMutation = `
      mutation CreateProduct($input: CreateProductInput!) {
        createProduct(input: $input) {
          id
          name
        }
      }
    `;

    const input = {
      id: p.id,
      name: p.name,
      category: p.category,
      description: descriptionString,
      imageUrls: [p.imageUrl],
      variants: formattedVariants,
    };

    try {
      await executeGraphQL(createMutation, { input });
      console.log(`✅ ${progress} Created: ${p.name} (${p.category}) - ${p.variants.length} sizes`);
      successCount++;
    } catch (err: any) {
      if (err.message && err.message.includes("DynamoDB:ConditionalCheckFailedException")) {
        // Already exists -> Update it
        const updateMutation = `
          mutation UpdateProduct($input: UpdateProductInput!) {
            updateProduct(input: $input) {
              id
              name
            }
          }
        `;
        try {
          await executeGraphQL(updateMutation, { input });
          console.log(`🔄 ${progress} Updated: ${p.name} (${p.category}) - ${p.variants.length} sizes`);
          successCount++;
        } catch (updateErr: any) {
          console.error(`❌ ${progress} Failed to update ${p.name}:`, updateErr.message);
          failCount++;
        }
      } else {
        console.error(`❌ ${progress} Failed to create ${p.name}:`, err.message);
        failCount++;
      }
    }
  }

  console.log(`\n======================================================`);
  console.log(`🎉 Seeding Finished!`);
  console.log(`   - Successfully Synced: ${successCount} products`);
  console.log(`   - Failed: ${failCount} products`);
  console.log(`======================================================\n`);
}

seedLiveCatalog().catch((e) => {
  console.error("Fatal Seeding Error:", e);
  process.exit(1);
});

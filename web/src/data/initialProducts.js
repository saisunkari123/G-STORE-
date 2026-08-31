export const INITIAL_CATEGORIES = [
  { id: 'rice', name: 'Rice', icon: '🌾', count: 1 },
  { id: 'oil', name: 'Oil', icon: '🛢️', count: 1 },
  { id: 'snacks', name: 'Snacks', icon: '🍿', count: 1 },
  { id: 'home-essentials', name: 'Home Essentials', icon: '🧼', count: 1 },
  { id: 'groceries', name: 'Groceries', icon: '🛒', count: 1 }
];

export const INITIAL_PRODUCTS = [
  {
    id: 'p-rice-1',
    name: 'BPT Sona Masoori Premium Raw Rice (25kg Bag)',
    category: 'Rice',
    description: 'Aged 12+ months, high aroma, zero adulteration, premium quality single-origin Andhra Sona Masoori rice.',
    mrp: 1450,
    price: 1320,
    unit: '25 kg Bag',
    imageUrl: 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80',
    stock: 45,
    isPopular: true,
    rating: 4.9,
    reviews: 128
  },
  {
    id: 'p-oil-1',
    name: 'Freedom Refined Sunflower Oil Pouch',
    category: 'Oil',
    description: 'Fortified with Vitamins A & D, heart-friendly, high smoke point pure refined sunflower oil for daily Indian cooking.',
    mrp: 145,
    price: 125,
    unit: '1 Litre Pouch',
    imageUrl: 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=600&q=80',
    stock: 80,
    isPopular: true,
    rating: 4.8,
    reviews: 94
  },
  {
    id: 'p-snacks-1',
    name: 'Kurkure Masala Munch Crispy Namkeen',
    category: 'Snacks',
    description: 'Authentic Indian spicy twisty namkeen made with rice, corn, and traditional spices for the ultimate tea-time crunch.',
    mrp: 30,
    price: 25,
    unit: '115g Pack',
    imageUrl: 'https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=600&q=80',
    stock: 120,
    isPopular: true,
    rating: 4.7,
    reviews: 210
  },
  {
    id: 'p-essentials-1',
    name: 'Surf Excel Quick Wash Detergent Powder',
    category: 'Home Essentials',
    description: 'Tough stain removal with X-Tra Clean Particles, dissolves fast in water and leaves clothes smelling fresh.',
    mrp: 180,
    price: 155,
    unit: '1 kg Pouch',
    imageUrl: 'https://images.unsplash.com/photo-1585670270606-25e4c0cfb49e?auto=format&fit=crop&w=600&q=80',
    stock: 50,
    isPopular: false,
    rating: 4.9,
    reviews: 67
  },
  {
    id: 'p-groceries-1',
    name: 'Tata Salt Vacuum Evaporated Iodized Salt',
    category: 'Groceries',
    description: 'Desh Ka Namak. Purity and guarantee of iodine for mental and physical development.',
    mrp: 30,
    price: 26,
    unit: '1 kg Pack',
    imageUrl: 'https://images.unsplash.com/photo-1518110925495-5fe2fda0442c?auto=format&fit=crop&w=600&q=80',
    stock: 95,
    isPopular: true,
    rating: 4.9,
    reviews: 143
  }
];

export const MILESTONE_GIFTS = [
  {
    id: 'gift-1',
    threshold: 499,
    title: 'Free Parle-G Gold Biscuit (150g)',
    giftPrice: 0,
    actualValue: 20,
    imageUrl: 'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=200&q=80'
  },
  {
    id: 'gift-2',
    threshold: 999,
    title: 'Fortune Premium Besan / Gram Flour (500g)',
    giftPrice: 0,
    actualValue: 65,
    imageUrl: 'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=200&q=80'
  },
  {
    id: 'gift-3',
    threshold: 1499,
    title: 'Dabur 100% Pure Honey Squeezy Pack (250g)',
    giftPrice: 0,
    actualValue: 120,
    imageUrl: 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?auto=format&fit=crop&w=200&q=80'
  }
];

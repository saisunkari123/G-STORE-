import React, { createContext, useContext, useState, useEffect } from 'react';
import { INITIAL_PRODUCTS, INITIAL_CATEGORIES, MILESTONE_GIFTS } from '../data/initialProducts';
import { STORE_HUB, calculateDistanceKm } from '../data/rajamLandmarks';

const StoreContext = createContext();

export function StoreProvider({ children }) {
  // Theme state
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('gstore_theme') || 'light';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('gstore_theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => (prev === 'light' ? 'dark' : 'light'));
  };

  // Products & Categories
  const [products, setProducts] = useState(() => {
    const saved = localStorage.getItem('gstore_products');
    return saved ? JSON.parse(saved) : INITIAL_PRODUCTS;
  });

  const [categories, setCategories] = useState(() => {
    const saved = localStorage.getItem('gstore_categories');
    return saved ? JSON.parse(saved) : INITIAL_CATEGORIES;
  });

  useEffect(() => {
    localStorage.setItem('gstore_products', JSON.stringify(products));
  }, [products]);

  useEffect(() => {
    localStorage.setItem('gstore_categories', JSON.stringify(categories));
  }, [categories]);

  // Active navigation tab & search
  const [activeTab, setActiveTab] = useState('SHOP'); // 'SHOP', 'ORDERS', 'ADMIN'
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Cart state: { [productId]: quantity }
  const [cart, setCart] = useState(() => {
    const saved = localStorage.getItem('gstore_cart');
    return saved ? JSON.parse(saved) : {};
  });

  useEffect(() => {
    localStorage.setItem('gstore_cart', JSON.stringify(cart));
  }, [cart]);

  const addToCart = (productId) => {
    setCart(prev => ({
      ...prev,
      [productId]: (prev[productId] || 0) + 1
    }));
  };

  const removeFromCart = (productId) => {
    setCart(prev => {
      const updated = { ...prev };
      if (updated[productId] > 1) {
        updated[productId] -= 1;
      } else {
        delete updated[productId];
      }
      return updated;
    });
  };

  const deleteFromCart = (productId) => {
    setCart(prev => {
      const updated = { ...prev };
      delete updated[productId];
      return updated;
    });
  };

  const clearCart = () => setCart({});

  // Active Delivery Address & Location Picker
  const [currentLocation, setCurrentLocation] = useState({
    name: 'More Supermarket, Main Road',
    area: 'Main Road, Rajam, Andhra Pradesh',
    lat: 18.4530,
    lng: 83.6545,
    distanceKm: 0.1,
    isDeliverable: true
  });

  const updateLocation = (loc) => {
    const distanceKm = calculateDistanceKm(STORE_HUB.lat, STORE_HUB.lng, loc.lat, loc.lng);
    const isDeliverable = distanceKm <= STORE_HUB.deliveryRadiusKm;
    setCurrentLocation({
      ...loc,
      distanceKm,
      isDeliverable
    });
  };

  // Modals & Drawers
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isRadiusModalOpen, setIsRadiusModalOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedGiftId, setSelectedGiftId] = useState(null);

  // Cart Calculations
  const cartItems = Object.entries(cart).map(([id, qty]) => {
    const product = products.find(p => p.id === id);
    return product ? { ...product, quantity: qty } : null;
  }).filter(Boolean);

  const cartTotalItems = Object.values(cart).reduce((sum, q) => sum + q, 0);
  const cartSubtotal = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const cartMrpTotal = cartItems.reduce((sum, item) => sum + (item.mrp * item.quantity), 0);
  const cartSavings = cartMrpTotal - cartSubtotal;
  const deliveryFee = cartSubtotal > 500 || cartSubtotal === 0 ? 0 : 30;
  const finalTotal = cartSubtotal + deliveryFee;

  // Unlocked milestone gifts
  const unlockedGifts = MILESTONE_GIFTS.filter(g => cartSubtotal >= g.threshold);
  const nextGift = MILESTONE_GIFTS.find(g => cartSubtotal < g.threshold);

  // Orders & Live Tracking
  const [orders, setOrders] = useState(() => {
    const saved = localStorage.getItem('gstore_orders');
    return saved ? JSON.parse(saved) : [
      {
        id: 'ORD-84920',
        date: new Date(Date.now() - 1000 * 60 * 15).toISOString(),
        items: [
          { name: 'BPT Sona Masoori Rice (25kg)', quantity: 1, price: 1320 },
          { name: 'Freedom Sunflower Oil (1L)', quantity: 2, price: 250 }
        ],
        subtotal: 1570,
        deliveryFee: 0,
        total: 1570,
        address: 'Dollars Colony, House #4-21, Rajam',
        status: 'DISPATCHED', // 'ORDERED', 'CONFIRMED', 'DISPATCHED', 'DELIVERED'
        paymentMethod: 'UPI'
      }
    ];
  });

  useEffect(() => {
    localStorage.setItem('gstore_orders', JSON.stringify(orders));
  }, [orders]);

  const placeOrder = (paymentMethod = 'Cash on Delivery') => {
    if (!currentLocation.isDeliverable) {
      alert(`Cannot place order: Selected address is outside our ${STORE_HUB.deliveryRadiusKm} km delivery zone.`);
      return null;
    }
    if (cartItems.length === 0) return null;

    const newOrder = {
      id: `ORD-${Math.floor(10000 + Math.random() * 90000)}`,
      date: new Date().toISOString(),
      items: cartItems.map(item => ({
        name: item.name,
        quantity: item.quantity,
        price: item.price
      })),
      subtotal: cartSubtotal,
      deliveryFee,
      total: finalTotal,
      address: `${currentLocation.name}, ${currentLocation.area}`,
      status: 'ORDERED',
      paymentMethod,
      gift: selectedGiftId ? MILESTONE_GIFTS.find(g => g.id === selectedGiftId)?.title : null
    };

    setOrders(prev => [newOrder, ...prev]);
    clearCart();
    setIsCartOpen(false);
    setActiveTab('ORDERS');
    return newOrder;
  };

  const cancelOrder = (orderId) => {
    setOrders(prev => prev.filter(o => o.id !== orderId));
  };

  // Admin Actions
  const addProduct = (newProduct) => {
    setProducts(prev => [
      {
        ...newProduct,
        id: `p-custom-${Date.now()}`,
        rating: 5.0,
        reviews: 1
      },
      ...prev
    ]);
  };

  const deleteProduct = (id) => {
    setProducts(prev => prev.filter(p => p.id !== id));
  };

  const updateProductStock = (id, newStock) => {
    setProducts(prev => prev.map(p => p.id === id ? { ...p, stock: Number(newStock) } : p));
  };

  const addCategory = (categoryName) => {
    const trimmed = categoryName.trim();
    if (!trimmed) return;
    const exists = categories.some(c => c.name.toLowerCase() === trimmed.toLowerCase());
    if (exists) return;
    const newCat = {
      id: trimmed.toLowerCase().replace(/\s+/g, '-'),
      name: trimmed,
      icon: '📦',
      count: 0
    };
    setCategories(prev => [...prev, newCat]);
  };

  return (
    <StoreContext.Provider
      value={{
        theme,
        toggleTheme,
        products,
        categories,
        activeTab,
        setActiveTab,
        selectedCategory,
        setSelectedCategory,
        searchQuery,
        setSearchQuery,
        cart,
        cartItems,
        cartTotalItems,
        cartSubtotal,
        cartMrpTotal,
        cartSavings,
        deliveryFee,
        finalTotal,
        addToCart,
        removeFromCart,
        deleteFromCart,
        clearCart,
        currentLocation,
        updateLocation,
        isCartOpen,
        setIsCartOpen,
        isRadiusModalOpen,
        setIsRadiusModalOpen,
        selectedProduct,
        setSelectedProduct,
        selectedGiftId,
        setSelectedGiftId,
        unlockedGifts,
        nextGift,
        orders,
        placeOrder,
        cancelOrder,
        addProduct,
        deleteProduct,
        updateProductStock,
        addCategory
      }}
    >
      {children}
    </StoreContext.Provider>
  );
}

export function useStore() {
  const context = useContext(StoreContext);
  if (!context) {
    throw new Error('useStore must be used within a StoreProvider');
  }
  return context;
}

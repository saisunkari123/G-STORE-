import React from 'react';
import { useStore } from '../context/StoreContext';
import { Store, ShoppingBag, ClipboardList, ShieldCheck } from 'lucide-react';

export default function BottomNav() {
  const { 
    activeTab, 
    setActiveTab, 
    cartTotalItems, 
    setIsCartOpen, 
    setIsRadiusModalOpen, 
    setSelectedProduct 
  } = useStore();

  const handleTabClick = (tab) => {
    setActiveTab(tab);
    setIsRadiusModalOpen(false);
    setSelectedProduct(null);
    if (tab === 'CART') {
      setIsCartOpen(true);
    } else {
      setIsCartOpen(false);
    }
  };

  return (
    <nav
      style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: 40,
        borderTop: '1px solid var(--border-color)'
      }}
      className="glass md:hidden"
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-around',
          padding: '8px 12px'
        }}
      >
        {/* Shop Tab */}
        <button
          onClick={() => handleTabClick('SHOP')}
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '2px',
            color: activeTab === 'SHOP' ? 'var(--primary)' : 'var(--text-muted)'
          }}
        >
          <Store size={20} />
          <span style={{ fontSize: '10.5px', fontWeight: activeTab === 'SHOP' ? '700' : '500' }}>
            Shop
          </span>
        </button>

        {/* Cart Tab */}
        <button
          onClick={() => handleTabClick('CART')}
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '2px',
            position: 'relative',
            color: 'var(--text-muted)'
          }}
        >
          <div style={{ position: 'relative' }}>
            <ShoppingBag size={20} />
            {cartTotalItems > 0 && (
              <span
                style={{
                  position: 'absolute',
                  top: '-4px',
                  right: '-6px',
                  background: 'var(--primary)',
                  color: '#FFFFFF',
                  fontSize: '9px',
                  fontWeight: '800',
                  padding: '1px 4px',
                  borderRadius: '10px'
                }}
              >
                {cartTotalItems}
              </span>
            )}
          </div>
          <span style={{ fontSize: '10.5px', fontWeight: '500' }}>
            Cart
          </span>
        </button>

        {/* Orders Tab */}
        <button
          onClick={() => handleTabClick('ORDERS')}
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '2px',
            color: activeTab === 'ORDERS' ? 'var(--primary)' : 'var(--text-muted)'
          }}
        >
          <ClipboardList size={20} />
          <span style={{ fontSize: '10.5px', fontWeight: activeTab === 'ORDERS' ? '700' : '500' }}>
            Orders
          </span>
        </button>

        {/* Admin Tab */}
        <button
          onClick={() => handleTabClick('ADMIN')}
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '2px',
            color: activeTab === 'ADMIN' ? '#D4AF37' : 'var(--text-muted)'
          }}
        >
          <ShieldCheck size={20} />
          <span style={{ fontSize: '10.5px', fontWeight: activeTab === 'ADMIN' ? '700' : '500' }}>
            Admin
          </span>
        </button>
      </div>
    </nav>
  );
}

import React, { useState } from 'react';
import { useStore } from '../context/StoreContext';
import MilestoneGifts from './MilestoneGifts';
import { 
  X, 
  Trash2, 
  Plus, 
  Minus, 
  ShoppingBag, 
  ArrowRight, 
  MapPin, 
  ShieldCheck, 
  Sparkles,
  CreditCard,
  Banknote,
  QrCode
} from 'lucide-react';

export default function CartDrawer() {
  const {
    isCartOpen,
    setIsCartOpen,
    cartItems,
    cartTotalItems,
    cartSubtotal,
    cartSavings,
    deliveryFee,
    finalTotal,
    addToCart,
    removeFromCart,
    deleteFromCart,
    currentLocation,
    setIsRadiusModalOpen,
    placeOrder
  } = useStore();

  const [paymentMethod, setPaymentMethod] = useState('UPI');

  if (!isCartOpen) return null;

  const handleCheckout = () => {
    if (!currentLocation.isDeliverable) {
      alert(`Cannot checkout: Your selected location (${currentLocation.distanceKm} km) is outside our 10 km Rajam delivery zone. Please choose an address in Rajam.`);
      setIsRadiusModalOpen(true);
      return;
    }
    const order = placeOrder(paymentMethod);
    if (order) {
      // Order placed successfully!
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 50,
        background: 'rgba(0, 0, 0, 0.65)',
        backdropFilter: 'blur(6px)',
        display: 'flex',
        justifyContent: 'flex-end'
      }}
      onClick={() => setIsCartOpen(false)}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: '100%',
          maxWidth: '460px',
          height: '100%',
          background: 'var(--bg-surface)',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: 'var(--shadow-xl)',
          borderLeft: '1px solid var(--border-color)',
          overflow: 'hidden'
        }}
        className="animate-slide"
      >
        {/* Drawer Header */}
        <div style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ShoppingBag size={20} color="var(--primary)" />
            <h3 style={{ fontSize: '17px', fontWeight: '800' }}>
              My Cart ({cartTotalItems} items)
            </h3>
          </div>
          <button
            onClick={() => setIsCartOpen(false)}
            style={{ width: '32px', height: '32px', borderRadius: '50%', background: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
          >
            <X size={16} />
          </button>
        </div>

        {/* Drawer Scrollable Content */}
        <div style={{ padding: '16px 20px', overflowY: 'auto', flex: 1, display: 'flex', flexDirection: 'column', gap: '14px' }}>
          
          {cartItems.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '60px 20px' }}>
              <span style={{ fontSize: '50px' }}>🛒</span>
              <h4 style={{ fontSize: '18px', fontWeight: '700', marginTop: '16px', marginBottom: '6px' }}>
                Your cart is empty
              </h4>
              <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '20px' }}>
                Add your favorite rice, oils, snacks & essentials to get started!
              </p>
              <button
                onClick={() => setIsCartOpen(false)}
                className="btn-primary"
              >
                Start Shopping
              </button>
            </div>
          ) : (
            <>
              {/* Delivery Address Pill */}
              <div
                onClick={() => setIsRadiusModalOpen(true)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '10px 14px',
                  background: currentLocation.isDeliverable ? 'var(--bg-subtle)' : 'var(--danger-light)',
                  border: `1px solid ${currentLocation.isDeliverable ? 'var(--border-color)' : 'var(--danger)'}`,
                  borderRadius: 'var(--radius-md)',
                  cursor: 'pointer'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <MapPin size={16} color={currentLocation.isDeliverable ? 'var(--primary)' : 'var(--danger)'} />
                  <div>
                    <p style={{ fontSize: '12.5px', fontWeight: '700', color: 'var(--text-primary)' }}>
                      Delivering to: {currentLocation.name.split(',')[0]}
                    </p>
                    <p style={{ fontSize: '11px', color: currentLocation.isDeliverable ? 'var(--primary)' : 'var(--danger)', fontWeight: '600' }}>
                      {currentLocation.isDeliverable ? `✓ Deliverable zone (~${currentLocation.distanceKm} km)` : `⚠️ Outside zone (~${currentLocation.distanceKm} km)`}
                    </p>
                  </div>
                </div>
                <span style={{ fontSize: '11px', fontWeight: '700', color: 'var(--primary)' }}>Change</span>
              </div>

              {/* Milestone Gifts Bar */}
              <MilestoneGifts />

              {/* Items List */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <p style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                  Cart Items
                </p>
                {cartItems.map((item) => (
                  <div
                    key={item.id}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '12px',
                      padding: '10px 12px',
                      background: 'var(--bg-surface)',
                      border: '1px solid var(--border-color)',
                      borderRadius: 'var(--radius-md)',
                      boxShadow: 'var(--shadow-sm)'
                    }}
                  >
                    <img
                      src={item.imageUrl}
                      alt={item.name}
                      style={{ width: '50px', height: '50px', borderRadius: '8px', objectFit: 'cover' }}
                    />
                    
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <p style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {item.name}
                      </p>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '2px' }}>
                        <span style={{ fontSize: '13px', fontWeight: '800', color: 'var(--primary)' }}>
                          ₹{item.price * item.quantity}
                        </span>
                        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                          (₹{item.price} each)
                        </span>
                      </div>
                    </div>

                    {/* Stepper */}
                    <div
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        background: 'var(--bg-subtle)',
                        borderRadius: 'var(--radius-full)',
                        padding: '3px 6px'
                      }}
                    >
                      <button
                        onClick={() => removeFromCart(item.id)}
                        style={{ width: '22px', height: '22px', borderRadius: '50%', background: 'var(--bg-surface)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                      >
                        <Minus size={12} />
                      </button>
                      <span style={{ fontSize: '12px', fontWeight: '800', minWidth: '16px', textAlign: 'center' }}>
                        {item.quantity}
                      </span>
                      <button
                        onClick={() => addToCart(item.id)}
                        style={{ width: '22px', height: '22px', borderRadius: '50%', background: 'var(--bg-surface)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                      >
                        <Plus size={12} />
                      </button>
                    </div>

                    {/* Delete button */}
                    <button
                      onClick={() => deleteFromCart(item.id)}
                      style={{ color: 'var(--text-muted)', padding: '4px' }}
                      title="Remove item"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                ))}
              </div>

              {/* Payment Method Selector */}
              <div style={{ marginTop: '6px' }}>
                <p style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '8px' }}>
                  Payment Method
                </p>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px' }}>
                  {[
                    { id: 'UPI', label: 'UPI / QR', icon: <QrCode size={16} /> },
                    { id: 'COD', label: 'Cash on Del.', icon: <Banknote size={16} /> },
                    { id: 'CARD', label: 'Card / Net', icon: <CreditCard size={16} /> }
                  ].map((pm) => (
                    <button
                      key={pm.id}
                      onClick={() => setPaymentMethod(pm.id)}
                      style={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        gap: '4px',
                        padding: '10px 6px',
                        borderRadius: 'var(--radius-md)',
                        background: paymentMethod === pm.id ? 'var(--primary-light)' : 'var(--bg-surface)',
                        border: `1px solid ${paymentMethod === pm.id ? 'var(--primary)' : 'var(--border-color)'}`,
                        color: paymentMethod === pm.id ? 'var(--primary)' : 'var(--text-secondary)',
                        fontWeight: '700',
                        fontSize: '11px',
                        cursor: 'pointer'
                      }}
                    >
                      {pm.icon}
                      <span>{pm.label}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Bill Details */}
              <div style={{ background: 'var(--bg-subtle)', borderRadius: 'var(--radius-md)', padding: '14px', marginTop: '6px' }}>
                <p style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', marginBottom: '10px', textTransform: 'uppercase' }}>
                  Bill Details
                </p>
                
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: '13px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Item Total</span>
                    <span style={{ fontWeight: '600' }}>₹{cartSubtotal}</span>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>Delivery Fee</span>
                    <span style={{ fontWeight: '600', color: deliveryFee === 0 ? 'var(--primary)' : 'inherit' }}>
                      {deliveryFee === 0 ? 'FREE' : `₹${deliveryFee}`}
                    </span>
                  </div>

                  {cartSavings > 0 && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--primary)', fontWeight: '700' }}>
                      <span>Total Savings</span>
                      <span>-₹{cartSavings}</span>
                    </div>
                  )}

                  <div style={{ borderTop: '1px dashed var(--border-color)', marginTop: '8px', paddingTop: '8px', display: 'flex', justifyContent: 'space-between', fontSize: '16px', fontWeight: '800' }}>
                    <span>To Pay</span>
                    <span style={{ color: 'var(--primary)', fontFamily: 'var(--font-heading)' }}>
                      ₹{finalTotal}
                    </span>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Drawer Footer Checkout Button */}
        {cartItems.length > 0 && (
          <div style={{ padding: '16px 20px', borderTop: '1px solid var(--border-color)', background: 'var(--bg-surface)' }}>
            <button
              onClick={handleCheckout}
              disabled={!currentLocation.isDeliverable}
              className="btn-primary"
              style={{ width: '100%', padding: '14px', borderRadius: 'var(--radius-md)', fontSize: '16px', fontWeight: '700' }}
            >
              <span>{currentLocation.isDeliverable ? `Place Order • ₹${finalTotal}` : 'Outside Delivery Zone'}</span>
              <ArrowRight size={18} />
            </button>
            <p style={{ fontSize: '11px', textAlign: 'center', color: 'var(--text-muted)', marginTop: '8px' }}>
              ⚡ 10-Min Fast Delivery Guarantee in Rajam
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

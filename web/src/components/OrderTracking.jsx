import React from 'react';
import { useStore } from '../context/StoreContext';
import { 
  Package, 
  CheckCircle2, 
  Clock, 
  Truck, 
  Home, 
  MapPin, 
  AlertCircle, 
  ShoppingBag,
  Gift
} from 'lucide-react';

export default function OrderTracking() {
  const { orders, cancelOrder, setActiveTab } = useStore();

  const STAGES = [
    { key: 'ORDERED', label: 'Ordered', icon: <Clock size={16} /> },
    { key: 'CONFIRMED', label: 'Confirmed', icon: <CheckCircle2 size={16} /> },
    { key: 'DISPATCHED', label: 'Dispatched', icon: <Truck size={16} /> },
    { key: 'DELIVERED', label: 'Delivered', icon: <Home size={16} /> }
  ];

  const getStageIndex = (status) => {
    switch (status) {
      case 'ORDERED': return 0;
      case 'CONFIRMED': return 1;
      case 'DISPATCHED': return 2;
      case 'DELIVERED': return 3;
      default: return 0;
    }
  };

  if (orders.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 20px' }}>
        <div style={{ width: '70px', height: '70px', borderRadius: '50%', background: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
          <Package size={36} color="var(--primary)" />
        </div>
        <h3 style={{ fontSize: '20px', fontWeight: '800', marginBottom: '8px' }}>
          No Active Orders
        </h3>
        <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '24px', maxWidth: '360px', margin: '0 auto 24px' }}>
          You haven't placed any orders yet. Explore our pantry staples and get instant 10-minute delivery in Rajam!
        </p>
        <button
          onClick={() => setActiveTab('SHOP')}
          className="btn-primary"
        >
          Explore Catalog
        </button>
      </div>
    );
  }

  return (
    <div style={{ padding: '24px 0 40px 0' }}>
      <div style={{ marginBottom: '24px' }}>
        <h2 style={{ fontSize: '22px', fontWeight: '800', marginBottom: '4px' }}>
          Order History & Live Status
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          Track your active grocery deliveries in real time
        </p>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        {orders.map((order) => {
          const currentStageIdx = getStageIndex(order.status);

          return (
            <div
              key={order.id}
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-xl)',
                padding: '20px',
                boxShadow: 'var(--shadow-md)'
              }}
            >
              {/* Top Header */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '10px', paddingBottom: '16px', borderBottom: '1px solid var(--border-color)' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontSize: '16px', fontWeight: '800', color: 'var(--text-primary)' }}>
                      {order.id}
                    </span>
                    <span className="badge badge-primary">
                      {order.status}
                    </span>
                  </div>
                  <p style={{ fontSize: '11.5px', color: 'var(--text-muted)', marginTop: '2px' }}>
                    Placed on {new Date(order.date).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}
                  </p>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '18px', fontWeight: '800', color: 'var(--primary)', fontFamily: 'var(--font-heading)' }}>
                    ₹{order.total}
                  </span>
                  <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                    Paid via {order.paymentMethod}
                  </p>
                </div>
              </div>

              {/* 4-Stage Stepper */}
              <div style={{ margin: '24px 0 20px 0' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', position: 'relative' }}>
                  {STAGES.map((stage, idx) => {
                    const isCompleted = idx <= currentStageIdx;
                    const isCurrent = idx === currentStageIdx;

                    return (
                      <div
                        key={stage.key}
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'center',
                          zIndex: 2,
                          flex: 1
                        }}
                      >
                        <div
                          style={{
                            width: '36px',
                            height: '36px',
                            borderRadius: '50%',
                            background: isCompleted ? 'var(--primary-gradient)' : 'var(--bg-subtle)',
                            color: isCompleted ? '#FFFFFF' : 'var(--text-muted)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            border: `2px solid ${isCompleted ? 'var(--primary)' : 'var(--border-color)'}`,
                            boxShadow: isCurrent ? 'var(--shadow-emerald)' : 'none',
                            transition: 'var(--transition)'
                          }}
                        >
                          {stage.icon}
                        </div>
                        <span
                          style={{
                            fontSize: '11.5px',
                            fontWeight: isCompleted ? '700' : '500',
                            color: isCompleted ? 'var(--text-primary)' : 'var(--text-muted)',
                            marginTop: '6px'
                          }}
                        >
                          {stage.label}
                        </span>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Delivery Address */}
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px', padding: '12px', background: 'var(--bg-subtle)', borderRadius: 'var(--radius-md)', marginBottom: '14px' }}>
                <MapPin size={16} color="var(--primary)" style={{ flexShrink: 0, marginTop: '2px' }} />
                <div>
                  <p style={{ fontSize: '11px', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                    Delivery Destination
                  </p>
                  <p style={{ fontSize: '13px', fontWeight: '600', color: 'var(--text-primary)' }}>
                    {order.address}
                  </p>
                </div>
              </div>

              {/* Items Summary */}
              <div style={{ marginBottom: '16px' }}>
                <p style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '8px' }}>
                  Ordered Items ({order.items.length})
                </p>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  {order.items.map((item, i) => (
                    <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                      <span style={{ color: 'var(--text-secondary)' }}>
                        {item.name} × <strong>{item.quantity}</strong>
                      </span>
                      <span style={{ fontWeight: '700', color: 'var(--text-primary)' }}>
                        ₹{item.price * item.quantity}
                      </span>
                    </div>
                  ))}

                  {order.gift && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#B8860B', fontSize: '12.5px', fontWeight: '700', marginTop: '4px' }}>
                      <Gift size={14} />
                      <span>Free Reward Gift: {order.gift}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Actions */}
              {order.status === 'ORDERED' && (
                <div style={{ display: 'flex', justifyContent: 'flex-end', paddingTop: '10px', borderTop: '1px dashed var(--border-color)' }}>
                  <button
                    onClick={() => {
                      if (confirm(`Are you sure you want to cancel ${order.id}?`)) {
                        cancelOrder(order.id);
                      }
                    }}
                    style={{ fontSize: '12.5px', fontWeight: '700', color: 'var(--danger)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                  >
                    <AlertCircle size={14} />
                    <span>Cancel Order</span>
                  </button>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

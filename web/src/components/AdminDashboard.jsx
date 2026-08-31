import React, { useState } from 'react';
import { useStore } from '../context/StoreContext';
import { 
  ShieldCheck, 
  PlusCircle, 
  Package, 
  TrendingUp, 
  ShoppingBag, 
  Users, 
  Trash2, 
  Edit3, 
  Check, 
  X, 
  Bike, 
  FolderPlus,
  AlertTriangle
} from 'lucide-react';

export default function AdminDashboard() {
  const { 
    products, 
    categories, 
    orders, 
    addProduct, 
    deleteProduct, 
    updateProductStock, 
    addCategory 
  } = useStore();

  // Form State
  const [formData, setFormData] = useState({
    name: '',
    category: categories[0]?.name || 'Rice',
    description: '',
    mrp: '',
    price: '',
    unit: '1 kg Pack',
    imageUrl: 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80',
    stock: '50'
  });

  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [newCatName, setNewCatName] = useState('');

  // Stats
  const totalRevenue = orders.reduce((sum, o) => sum + o.total, 0);
  const totalItemsSold = orders.reduce((sum, o) => sum + o.items.reduce((s, i) => s + i.quantity, 0), 0);

  const handleCreateProduct = (e) => {
    e.preventDefault();
    setFormError('');
    setFormSuccess('');

    // Required Field Validations
    if (!formData.name.trim()) {
      setFormError('Product Name is required *');
      return;
    }
    if (!formData.category.trim()) {
      setFormError('Category is required *');
      return;
    }
    const mrpNum = Number(formData.mrp);
    const priceNum = Number(formData.price);
    const stockNum = Number(formData.stock);

    if (isNaN(mrpNum) || mrpNum <= 0) {
      setFormError('Valid MRP greater than 0 is required *');
      return;
    }
    if (isNaN(priceNum) || priceNum <= 0) {
      setFormError('Valid Sale Price greater than 0 is required *');
      return;
    }
    if (priceNum > mrpNum) {
      setFormError('Sale Price cannot be greater than MRP');
      return;
    }
    if (isNaN(stockNum) || stockNum < 0) {
      setFormError('Initial Quantity is required *');
      return;
    }
    if (!formData.imageUrl.trim()) {
      setFormError('Product Image is required *');
      return;
    }

    addProduct({
      name: formData.name.trim(),
      category: formData.category,
      description: formData.description.trim() || `${formData.name.trim()} - Premium quality grocery essential from G-Store Rajam.`,
      mrp: mrpNum,
      price: priceNum,
      unit: formData.unit.trim() || '1 Unit',
      imageUrl: formData.imageUrl.trim(),
      stock: stockNum,
      isPopular: true
    });

    setFormSuccess(`✓ Product "${formData.name}" added successfully!`);
    setFormData({
      name: '',
      category: categories[0]?.name || 'Rice',
      description: '',
      mrp: '',
      price: '',
      unit: '1 kg Pack',
      imageUrl: 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80',
      stock: '50'
    });

    setTimeout(() => setFormSuccess(''), 4000);
  };

  const handleAddCategorySubmit = (e) => {
    e.preventDefault();
    if (!newCatName.trim()) return;
    addCategory(newCatName.trim());
    setFormData(prev => ({ ...prev, category: newCatName.trim() }));
    setNewCatName('');
    setIsCategoryModalOpen(false);
  };

  return (
    <div style={{ padding: '24px 0 40px 0' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px', flexWrap: 'wrap', gap: '10px' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ShieldCheck size={24} color="var(--primary)" />
            <span>Admin Store Control Panel</span>
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
            Real-time inventory management, stock controls & delivery fleet
          </p>
        </div>
        <span className="badge badge-gold" style={{ padding: '6px 12px', fontSize: '12px' }}>
          G-Store Rajam Central
        </span>
      </div>

      {/* KPI Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '14px', marginBottom: '28px' }}>
        <div style={{ background: 'var(--bg-surface)', padding: '16px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-sm)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-muted)' }}>TOTAL REVENUE</span>
            <TrendingUp size={18} color="var(--primary)" />
          </div>
          <p style={{ fontSize: '24px', fontWeight: '800', color: 'var(--primary)', marginTop: '6px', fontFamily: 'var(--font-heading)' }}>
            ₹{totalRevenue}
          </p>
        </div>

        <div style={{ background: 'var(--bg-surface)', padding: '16px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-sm)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-muted)' }}>TOTAL ORDERS</span>
            <ShoppingBag size={18} color="#D4AF37" />
          </div>
          <p style={{ fontSize: '24px', fontWeight: '800', color: 'var(--text-primary)', marginTop: '6px', fontFamily: 'var(--font-heading)' }}>
            {orders.length}
          </p>
        </div>

        <div style={{ background: 'var(--bg-surface)', padding: '16px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-sm)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-muted)' }}>INVENTORY ITEMS</span>
            <Package size={18} color="var(--primary)" />
          </div>
          <p style={{ fontSize: '24px', fontWeight: '800', color: 'var(--text-primary)', marginTop: '6px', fontFamily: 'var(--font-heading)' }}>
            {products.length}
          </p>
        </div>

        <div style={{ background: 'var(--bg-surface)', padding: '16px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-sm)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-muted)' }}>CATEGORIES</span>
            <FolderPlus size={18} color="#D4AF37" />
          </div>
          <p style={{ fontSize: '24px', fontWeight: '800', color: 'var(--text-primary)', marginTop: '6px', fontFamily: 'var(--font-heading)' }}>
            {categories.length}
          </p>
        </div>
      </div>

      {/* Main Two Columns: Add Product & Fleet Overview */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '24px', marginBottom: '32px' }}>
        
        {/* Add Product Form with Required Stars */}
        <div style={{ background: 'var(--bg-surface)', padding: '20px', borderRadius: 'var(--radius-xl)', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-md)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <PlusCircle size={18} color="var(--primary)" />
              <span>Add New Product</span>
            </h3>
            <span style={{ fontSize: '11px', color: 'var(--danger)', fontWeight: '700' }}>* Required fields</span>
          </div>

          {formError && (
            <div style={{ padding: '10px 14px', background: 'var(--danger-light)', border: '1px solid var(--danger)', borderRadius: 'var(--radius-sm)', color: 'var(--danger)', fontSize: '12.5px', fontWeight: '700', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <AlertTriangle size={16} />
              <span>{formError}</span>
            </div>
          )}

          {formSuccess && (
            <div style={{ padding: '10px 14px', background: 'var(--primary-light)', border: '1px solid var(--primary)', borderRadius: 'var(--radius-sm)', color: 'var(--primary)', fontSize: '12.5px', fontWeight: '700', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Check size={16} />
              <span>{formSuccess}</span>
            </div>
          )}

          <form onSubmit={handleCreateProduct} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {/* Product Name */}
            <div>
              <label style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                Product Name <span style={{ color: 'var(--danger)' }}>*</span>
              </label>
              <input
                type="text"
                placeholder="e.g. Sona Masoori Rice, Freedom Oil..."
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontSize: '13px' }}
              />
            </div>

            {/* Category with Dropdown + Inline Creator */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                <label style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)' }}>
                  Category <span style={{ color: 'var(--danger)' }}>*</span>
                </label>
                <button
                  type="button"
                  onClick={() => setIsCategoryModalOpen(true)}
                  style={{ fontSize: '11px', fontWeight: '700', color: 'var(--primary)' }}
                >
                  + Add New Category
                </button>
              </div>

              <select
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontSize: '13px' }}
              >
                {categories.map((c) => (
                  <option key={c.id || c.name} value={c.name}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            {/* MRP & Sale Price Row */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                  MRP (₹) <span style={{ color: 'var(--danger)' }}>*</span>
                </label>
                <input
                  type="number"
                  placeholder="e.g. 150"
                  value={formData.mrp}
                  onChange={(e) => setFormData({ ...formData, mrp: e.target.value })}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontSize: '13px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                  Sale Price (₹) <span style={{ color: 'var(--danger)' }}>*</span>
                </label>
                <input
                  type="number"
                  placeholder="e.g. 130"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontSize: '13px' }}
                />
              </div>
            </div>

            {/* Unit & Initial Quantity */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                  Unit / Pack Size <span style={{ color: 'var(--danger)' }}>*</span>
                </label>
                <input
                  type="text"
                  placeholder="e.g. 1 kg / 1L Pouch"
                  value={formData.unit}
                  onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontSize: '13px' }}
                />
              </div>

              <div>
                <label style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                  Initial Quantity <span style={{ color: 'var(--danger)' }}>*</span>
                </label>
                <input
                  type="number"
                  placeholder="e.g. 50"
                  value={formData.stock}
                  onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontSize: '13px' }}
                />
              </div>
            </div>

            {/* Product Image URL */}
            <div>
              <label style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', display: 'block', marginBottom: '4px' }}>
                Product Image URL <span style={{ color: 'var(--danger)' }}>*</span>
              </label>
              <input
                type="url"
                placeholder="https://..."
                value={formData.imageUrl}
                onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontSize: '13px' }}
              />
            </div>

            <button
              type="submit"
              className="btn-primary"
              style={{ marginTop: '8px', padding: '12px', borderRadius: 'var(--radius-md)', fontSize: '14px' }}
            >
              <PlusCircle size={16} />
              <span>Save & Publish to Catalog</span>
            </button>
          </form>
        </div>

        {/* Fleet & Delivery Management */}
        <div style={{ background: 'var(--bg-surface)', padding: '20px', borderRadius: 'var(--radius-xl)', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-md)', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Bike size={18} color="var(--primary)" />
              <span>Rajam Delivery Fleet (3 Active)</span>
            </h3>
            <span className="badge badge-success">GPS Online</span>
          </div>

          <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '16px' }}>
            Live status of active field delivery partners across Rajam zones:
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', flex: 1 }}>
            {[
              { name: 'Raju (Rajam Hub)', phone: '+91 98480 12345', status: 'Delivering to Dollars Colony', time: '5 mins away', color: 'var(--primary)' },
              { name: 'Suresh (RTC Complex)', phone: '+91 98480 67890', status: 'At Store - Ready for Dispatch', time: 'Idle', color: 'var(--success)' },
              { name: 'Kiran (GMRIT Area)', phone: '+91 98480 54321', status: 'Delivered Order #ORD-84920', time: 'Completed', color: 'var(--secondary)' }
            ].map((driver, idx) => (
              <div
                key={idx}
                style={{
                  padding: '12px',
                  background: 'var(--bg-subtle)',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-color)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: 'var(--primary-light)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Bike size={18} color="var(--primary)" />
                  </div>
                  <div>
                    <p style={{ fontSize: '13px', fontWeight: '700' }}>{driver.name}</p>
                    <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{driver.status}</p>
                  </div>
                </div>
                <span className="badge" style={{ background: 'rgba(0,0,0,0.05)', fontSize: '10.5px' }}>
                  {driver.time}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Inventory Stock Management Table */}
      <div style={{ background: 'var(--bg-surface)', padding: '20px', borderRadius: 'var(--radius-xl)', border: '1px solid var(--border-color)', boxShadow: 'var(--shadow-md)' }}>
        <h3 style={{ fontSize: '16px', fontWeight: '800', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <Package size={18} color="var(--primary)" />
          <span>Live Catalog & Stock Management ({products.length} items)</span>
        </h3>

        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)' }}>
                <th style={{ padding: '10px 8px' }}>Product</th>
                <th style={{ padding: '10px 8px' }}>Category</th>
                <th style={{ padding: '10px 8px' }}>MRP</th>
                <th style={{ padding: '10px 8px' }}>Sale Price</th>
                <th style={{ padding: '10px 8px' }}>Stock Units</th>
                <th style={{ padding: '10px 8px', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  <td style={{ padding: '12px 8px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <img src={p.imageUrl} alt={p.name} style={{ width: '38px', height: '38px', borderRadius: '6px', objectFit: 'cover' }} />
                    <span style={{ fontWeight: '700', maxWidth: '240px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {p.name}
                    </span>
                  </td>
                  <td style={{ padding: '12px 8px' }}>
                    <span className="badge badge-primary">{p.category}</span>
                  </td>
                  <td style={{ padding: '12px 8px', color: 'var(--text-muted)', textDecoration: 'line-through' }}>
                    ₹{p.mrp}
                  </td>
                  <td style={{ padding: '12px 8px', fontWeight: '800', color: 'var(--primary)' }}>
                    ₹{p.price}
                  </td>
                  <td style={{ padding: '12px 8px' }}>
                    <input
                      type="number"
                      defaultValue={p.stock}
                      onBlur={(e) => updateProductStock(p.id, e.target.value)}
                      style={{ width: '70px', padding: '4px 8px', borderRadius: '4px', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontWeight: '700' }}
                    />
                  </td>
                  <td style={{ padding: '12px 8px', textAlign: 'right' }}>
                    <button
                      onClick={() => {
                        if (confirm(`Delete "${p.name}" from catalog?`)) {
                          deleteProduct(p.id);
                        }
                      }}
                      style={{ color: 'var(--danger)', padding: '6px' }}
                      title="Delete product"
                    >
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* New Category Modal */}
      {isCategoryModalOpen && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            zIndex: 60,
            background: 'rgba(0,0,0,0.6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '16px'
          }}
        >
          <div
            style={{
              background: 'var(--bg-surface)',
              borderRadius: 'var(--radius-lg)',
              padding: '24px',
              maxWidth: '380px',
              width: '100%',
              boxShadow: 'var(--shadow-xl)',
              border: '1px solid var(--border-color)'
            }}
          >
            <h4 style={{ fontSize: '16px', fontWeight: '800', marginBottom: '8px' }}>
              Create New Category
            </h4>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '14px' }}>
              Add a new category for products in the G-Store catalog.
            </p>
            <form onSubmit={handleAddCategorySubmit}>
              <input
                type="text"
                placeholder="e.g. Spices, Dairy, Beverages..."
                value={newCatName}
                onChange={(e) => setNewCatName(e.target.value)}
                autoFocus
                style={{ width: '100%', padding: '9px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', background: 'var(--bg-subtle)', fontSize: '13px', marginBottom: '14px' }}
              />
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  type="button"
                  onClick={() => setIsCategoryModalOpen(false)}
                  className="btn-secondary"
                  style={{ flex: 1 }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  style={{ flex: 1 }}
                >
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

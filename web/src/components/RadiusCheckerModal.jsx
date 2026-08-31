import React, { useState, useEffect } from 'react';
import { useStore } from '../context/StoreContext';
import { STORE_HUB, searchLivePlaces, calculateDistanceKm } from '../data/rajamLandmarks';
import { X, MapPin, Search, CheckCircle2, AlertTriangle, Building2, Loader2 } from 'lucide-react';

export default function RadiusCheckerModal() {
  const { isRadiusModalOpen, setIsRadiusModalOpen, currentLocation, updateLocation } = useStore();
  
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedLoc, setSelectedLoc] = useState(currentLocation);

  useEffect(() => {
    if (!searchTerm.trim() || searchTerm.trim().length < 2) {
      setSearchResults([]);
      return;
    }

    const timer = setTimeout(async () => {
      setIsLoading(true);
      const results = await searchLivePlaces(searchTerm);
      setSearchResults(results);
      setIsLoading(false);
    }, 400);

    return () => clearTimeout(timer);
  }, [searchTerm]);

  if (!isRadiusModalOpen) return null;

  const handleSelect = (loc) => {
    setSelectedLoc(loc);
  };

  const handleConfirm = () => {
    if (!selectedLoc.isDeliverable) {
      alert(`Cannot set address: Selected location (${selectedLoc.distanceKm} km) is outside our ${STORE_HUB.deliveryRadiusKm} km delivery radius.`);
      return;
    }
    updateLocation(selectedLoc);
    setIsRadiusModalOpen(false);
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
        alignItems: 'center',
        justifyContent: 'center',
        padding: '16px'
      }}
      onClick={() => setIsRadiusModalOpen(false)}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: 'var(--bg-surface)',
          borderRadius: 'var(--radius-xl)',
          maxWidth: '540px',
          width: '100%',
          maxHeight: '90vh',
          display: 'flex',
          flexDirection: 'column',
          boxShadow: 'var(--shadow-xl)',
          border: '1px solid var(--border-color)',
          overflow: 'hidden'
        }}
        className="animate-fade"
      >
        {/* Modal Header */}
        <div style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <h3 style={{ fontSize: '17px', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <MapPin size={18} color="var(--primary)" />
              <span>Delivery Radius & Location</span>
            </h3>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              Rajam Central Store Hub • Max {STORE_HUB.deliveryRadiusKm} km Express Zone
            </p>
          </div>
          <button
            onClick={() => setIsRadiusModalOpen(false)}
            style={{ width: '32px', height: '32px', borderRadius: '50%', background: 'var(--bg-subtle)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
          >
            <X size={16} />
          </button>
        </div>

        {/* Content Body */}
        <div style={{ padding: '16px 20px', overflowY: 'auto', flex: 1, display: 'flex', flexDirection: 'column', gap: '14px' }}>
          
          {/* Live Search Box */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              background: 'var(--bg-subtle)',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius-md)',
              padding: '8px 12px'
            }}
          >
            {isLoading ? <Loader2 size={16} className="animate-spin" color="var(--primary)" /> : <Search size={16} color="var(--text-muted)" />}
            <input
              type="text"
              placeholder="Search any colony, street, village or landmark..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                border: 'none',
                outline: 'none',
                background: 'transparent',
                width: '100%',
                fontSize: '13px',
                color: 'var(--text-primary)'
              }}
            />
            {searchTerm && (
              <button onClick={() => setSearchTerm('')}>
                <X size={14} color="var(--text-muted)" />
              </button>
            )}
          </div>

          {/* Current Selection Deliverability Card */}
          <div
            style={{
              padding: '14px',
              borderRadius: 'var(--radius-md)',
              background: selectedLoc.isDeliverable ? 'var(--primary-light)' : 'var(--danger-light)',
              border: `1px solid ${selectedLoc.isDeliverable ? 'var(--primary)' : 'var(--danger)'}`
            }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '12px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  {selectedLoc.isDeliverable ? (
                    <CheckCircle2 size={18} color="var(--primary)" />
                  ) : (
                    <AlertTriangle size={18} color="var(--danger)" />
                  )}
                  <span style={{ fontSize: '14px', fontWeight: '800', color: selectedLoc.isDeliverable ? 'var(--primary)' : 'var(--danger)' }}>
                    {selectedLoc.isDeliverable ? '✓ Within Delivery Zone' : '⚠️ Outside Delivery Zone'}
                  </span>
                </div>
                <p style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', marginTop: '4px' }}>
                  {selectedLoc.name}
                </p>
                <p style={{ fontSize: '11.5px', color: 'var(--text-secondary)' }}>
                  {selectedLoc.area}
                </p>
              </div>

              <span
                style={{
                  padding: '4px 10px',
                  borderRadius: 'var(--radius-full)',
                  fontSize: '12px',
                  fontWeight: '800',
                  background: selectedLoc.isDeliverable ? 'var(--primary)' : 'var(--danger)',
                  color: '#FFFFFF',
                  flexShrink: 0
                }}
              >
                ~{selectedLoc.distanceKm} km
              </span>
            </div>

            <p style={{ fontSize: '11px', marginTop: '8px', color: selectedLoc.isDeliverable ? 'var(--primary)' : 'var(--danger)' }}>
              {selectedLoc.isDeliverable
                ? `Fast 10-minute delivery available from Rajam hub.`
                : `We currently deliver within ${STORE_HUB.deliveryRadiusKm} km only. Addresses outside this radius cannot be serviced.`}
            </p>
          </div>

          {/* Dynamic Live Search Results */}
          {searchResults.length > 0 && (
            <div>
              <p style={{ fontSize: '12px', fontWeight: '700', color: 'var(--text-secondary)', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                Live Search Matches
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', maxHeight: '220px', overflowY: 'auto' }}>
                {searchResults.map((loc, idx) => {
                  const isCur = selectedLoc.name === loc.name;

                  return (
                    <div
                      key={idx}
                      onClick={() => handleSelect(loc)}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '10px 12px',
                        borderRadius: 'var(--radius-sm)',
                        background: isCur ? 'var(--bg-subtle)' : 'var(--bg-surface)',
                        border: `1px solid ${isCur ? 'var(--primary)' : 'var(--border-color)'}`,
                        cursor: 'pointer',
                        transition: 'var(--transition)'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', minWidth: 0, flex: 1, paddingRight: '8px' }}>
                        <Building2 size={16} color={loc.isDeliverable ? 'var(--primary)' : 'var(--danger)'} style={{ flexShrink: 0 }} />
                        <div style={{ minWidth: 0 }}>
                          <p style={{ fontSize: '13px', fontWeight: '700', color: 'var(--text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {loc.name}
                          </p>
                          <p style={{ fontSize: '11px', color: 'var(--text-muted)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {loc.area}
                          </p>
                        </div>
                      </div>

                      <div style={{ textAlign: 'right', flexShrink: 0 }}>
                        <span
                          style={{
                            fontSize: '11px',
                            fontWeight: '700',
                            color: loc.isDeliverable ? 'var(--primary)' : 'var(--danger)'
                          }}
                        >
                          ~{loc.distanceKm} km
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div style={{ padding: '14px 20px', borderTop: '1px solid var(--border-color)', display: 'flex', gap: '10px' }}>
          <button
            onClick={() => setIsRadiusModalOpen(false)}
            className="btn-secondary"
            style={{ flex: 1 }}
          >
            Cancel
          </button>
          <button
            onClick={handleConfirm}
            disabled={!selectedLoc.isDeliverable}
            className="btn-primary"
            style={{ flex: 2 }}
          >
            {selectedLoc.isDeliverable ? 'Confirm Location' : 'Outside Zone (Cannot Deliver)'}
          </button>
        </div>
      </div>
    </div>
  );
}

'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { apiFetch } from '@/services/api';

interface ClientItem {
  id: string;
  companyId: string;
  fullName: string;
  passportNumber: string;
  nationality: string;
  dateOfBirth: string;
  phone: string | null;
  email: string | null;
  address: string | null;
  createdAt: string;
}

export default function ClientsPage() {
  const [clients, setClients] = useState<ClientItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Delete state
  const [deleteTarget, setDeleteTarget] = useState<ClientItem | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const loadClients = async (query = '') => {
    setLoading(true);
    setError('');
    try {
      const url = query.trim()
        ? `/clients?search=${encodeURIComponent(query.trim())}&size=50`
        : '/clients?size=50';
      const res = await apiFetch<any>(url);
      setClients(res.content || []);
    } catch (err: any) {
      setError(err.message || 'Failed to retrieve client records.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      loadClients(searchQuery);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  const handleDeleteClient = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await apiFetch(`/clients/${deleteTarget.id}`, { method: 'DELETE' });
      setClients((prev) => prev.filter((c) => c.id !== deleteTarget.id));
      setSuccessMsg(`Client "${deleteTarget.fullName}" deleted successfully.`);
      setDeleteTarget(null);
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (err: any) {
      setDeleteError(err.message || 'Failed to delete client record.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '1.8rem', color: '#fff', marginBottom: '4px' }}>Client Directory</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Manage client profiles and view associated visa application histories
          </p>
        </div>
      </div>

      {successMsg && (
        <div style={{ padding: '12px 16px', background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.2)', borderRadius: '10px', color: 'var(--color-success)', fontSize: '0.9rem' }}>
          ✓ {successMsg}
        </div>
      )}

      {error && (
        <div style={{ padding: '12px 16px', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: '10px', color: 'var(--color-danger)', fontSize: '0.9rem' }}>
          {error}
        </div>
      )}

      {/* Search Bar */}
      <div className="glass-card" style={{ padding: '16px 24px', display: 'flex', alignItems: 'center', gap: '16px' }}>
        <span style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>🔍</span>
        <input
          type="text"
          className="form-input"
          placeholder="Search clients by name or passport number…"
          style={{ flex: 1, padding: '10px 14px', fontSize: '0.9rem' }}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        {searchQuery && (
          <button className="btn-secondary" style={{ padding: '6px 12px', fontSize: '0.8rem' }} onClick={() => setSearchQuery('')}>
            Clear
          </button>
        )}
      </div>

      {/* Clients Table */}
      <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            Loading client profiles…
          </div>
        ) : clients.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            {searchQuery ? 'No clients found matching your search.' : 'No clients registered yet.'}
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ background: 'rgba(255, 255, 255, 0.02)', borderBottom: '1px solid var(--glass-border)', color: 'var(--text-muted)', fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                <th style={{ padding: '16px 24px' }}>Client Name</th>
                <th style={{ padding: '16px 24px' }}>Passport Number</th>
                <th style={{ padding: '16px 24px' }}>Nationality</th>
                <th style={{ padding: '16px 24px' }}>Date of Birth</th>
                <th style={{ padding: '16px 24px' }}>Contact</th>
                <th style={{ padding: '16px 24px', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {clients.map((c) => (
                <tr key={c.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.03)', transition: 'background 0.2s ease' }}>
                  <td style={{ padding: '16px 24px', fontWeight: 600, color: '#fff' }}>
                    <Link href={`/dashboard/clients/${c.id}`} style={{ color: '#fff', textDecoration: 'none' }}>
                      {c.fullName}
                    </Link>
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--primary)', fontWeight: 600, fontFamily: 'monospace' }}>
                    {c.passportNumber}
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-main)' }}>
                    {c.nationality}
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    {c.dateOfBirth}
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    {c.phone || c.email || '—'}
                  </td>
                  <td style={{ padding: '16px 24px', textAlign: 'right', display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                    <Link href={`/dashboard/clients/${c.id}`} className="btn-secondary" style={{ padding: '6px 12px', fontSize: '0.8rem', textDecoration: 'none' }}>
                      View Profile →
                    </Link>
                    <button
                      className="btn-danger"
                      style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                      onClick={() => {
                        setDeleteTarget(c);
                        setDeleteError('');
                      }}
                    >
                      🗑️ Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {deleteTarget && (
        <div style={modalStyles.overlay} onClick={() => !deleting && setDeleteTarget(null)}>
          <div className="glass-card" style={modalStyles.modal} onClick={(e) => e.stopPropagation()}>
            <h3 style={{ color: 'var(--color-danger)', fontSize: '1.25rem', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              ⚠️ Confirm Client Deletion
            </h3>

            <p style={{ color: 'var(--text-main)', fontSize: '0.95rem', marginBottom: '12px', lineHeight: 1.5 }}>
              Are you sure you want to delete client <strong style={{ color: '#fff' }}>{deleteTarget.fullName}</strong> (Passport: {deleteTarget.passportNumber})?
            </p>

            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '20px', background: 'rgba(239, 68, 68, 0.08)', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '10px 14px', borderRadius: '8px', lineHeight: 1.4 }}>
              <strong>Warning:</strong> Deleting this client profile will also permanently delete all associated visa applications, uploaded documents, stage histories, and case notes. This action cannot be undone.
            </p>

            {deleteError && (
              <div style={{ padding: '10px 14px', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: '8px', color: 'var(--color-danger)', fontSize: '0.85rem', marginBottom: 16 }}>
                {deleteError}
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
              <button type="button" className="btn-secondary" onClick={() => setDeleteTarget(null)} disabled={deleting}>
                Cancel
              </button>
              <button type="button" className="btn-danger" onClick={handleDeleteClient} disabled={deleting}>
                {deleting ? 'Deleting Client…' : 'Yes, Delete Client'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

const modalStyles: Record<string, React.CSSProperties> = {
  overlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: 'rgba(0,0,0,0.75)',
    backdropFilter: 'blur(8px)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 200,
  },
  modal: {
    width: '100%',
    maxWidth: 500,
    padding: 28,
    borderRadius: 20,
  },
};

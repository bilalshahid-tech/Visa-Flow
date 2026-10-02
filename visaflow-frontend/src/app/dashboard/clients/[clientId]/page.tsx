'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { apiFetch } from '@/services/api';
import { NationalitySelect, PhoneInputWithCode, parsePhoneString, combinePhone } from '@/components/ClientFormInputs';

interface ClientCaseSummary {
  id: string;
  caseReference: string;
  visaProgramName: string | null;
  visaTypeName: string | null;
  status: string;
  currentStageName: string | null;
  createdAt: string;
}

interface ClientDetail {
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
  updatedAt: string;
  cases: ClientCaseSummary[];
}

export default function ClientProfilePage() {
  const router = useRouter();
  const { clientId } = useParams() as { clientId: string };
  const [client, setClient] = useState<ClientDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Edit modal state
  const [showEditModal, setShowEditModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editError, setEditError] = useState('');
  const [phoneCode, setPhoneCode] = useState('+92');
  const [editForm, setEditForm] = useState({
    fullName: '',
    passportNumber: '',
    nationality: '',
    dateOfBirth: '',
    phone: '',
    email: '',
    address: '',
  });

  // Delete modal state
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const loadClient = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await apiFetch<ClientDetail>(`/clients/${clientId}`, { cache: 'no-store' } as any);
      setClient(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load client profile.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (clientId) {
      loadClient();
    }
  }, [clientId]);

  const openEditModal = () => {
    if (!client) return;
    const parsedPhone = parsePhoneString(client.phone);
    setPhoneCode(parsedPhone.code);
    setEditForm({
      fullName: client.fullName || '',
      passportNumber: client.passportNumber || '',
      nationality: client.nationality || 'Pakistani',
      dateOfBirth: client.dateOfBirth || '',
      phone: parsedPhone.number,
      email: client.email || '',
      address: client.address || '',
    });
    setEditError('');
    setShowEditModal(true);
  };

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setEditError('');
    try {
      const updated = await apiFetch<ClientDetail>(`/clients/${clientId}`, {
        method: 'PUT',
        bodyData: {
          fullName: editForm.fullName.trim(),
          passportNumber: editForm.passportNumber.trim(),
          nationality: editForm.nationality.trim(),
          dateOfBirth: editForm.dateOfBirth,
          phone: combinePhone(phoneCode, editForm.phone) || null,
          email: editForm.email.trim() || null,
          address: editForm.address.trim() || null,
        },
      });
      setClient(updated);
      setShowEditModal(false);
    } catch (err: any) {
      setEditError(err.message || 'Failed to update client details.');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteClient = async () => {
    if (!client) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await apiFetch(`/clients/${clientId}`, { method: 'DELETE' });
      router.push('/dashboard/clients');
    } catch (err: any) {
      setDeleteError(err.message || 'Failed to delete client.');
      setDeleting(false);
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'Approved':
      case 'APPROVED': return 'badge-low';
      case 'Rejected':
      case 'REJECTED': return 'badge-high';
      case 'Submitted':
      case 'SUBMITTED': return 'badge-critical';
      case 'Under Review':
      case 'UNDER_REVIEW':
      case 'Documents Pending':
      case 'DOCS_PENDING': return 'badge-medium';
      default: return 'badge-secondary';
    }
  };

  if (loading) {
    return <div style={{ color: 'var(--text-muted)', padding: '60px', textAlign: 'center' }}>Loading client profile…</div>;
  }

  if (error || !client) {
    return (
      <div style={{ padding: '40px', textAlign: 'center' }}>
        <p style={{ color: 'var(--color-danger)', marginBottom: 16 }}>{error || 'Client not found.'}</p>
        <Link href="/dashboard/clients" className="btn-secondary" style={{ textDecoration: 'none' }}>
          ← Back to Clients
        </Link>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <Link href="/dashboard/clients" className="btn-secondary" style={{ padding: '8px 16px', fontSize: '0.85rem', textDecoration: 'none' }}>
            ← Clients
          </Link>
          <div>
            <h1 style={{ fontSize: '1.8rem', color: '#fff', marginBottom: '4px' }}>{client.fullName}</h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
              Passport: <span style={{ color: '#fff', fontWeight: 600 }}>{client.passportNumber}</span> · Nationality: <span style={{ color: '#fff', fontWeight: 600 }}>{client.nationality}</span>
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <button className="btn-secondary" style={{ padding: '10px 20px', fontSize: '0.9rem' }} onClick={openEditModal}>
            ✏️ Edit Profile
          </button>
          <button
            className="btn-danger"
            style={{ padding: '10px 20px', fontSize: '0.9rem' }}
            onClick={() => {
              setDeleteError('');
              setShowDeleteModal(true);
            }}
          >
            🗑️ Delete Client
          </button>
        </div>
      </div>

      {/* Main Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1.6fr)', gap: '24px' }}>
        {/* Left Column: Personal Information */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: 12 }}>
            <h2 style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 600 }}>👤 Personal Details</h2>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              Registered {new Date(client.createdAt).toLocaleDateString()}
            </span>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div>
              <div style={s.label}>Full Name</div>
              <div style={s.val}>{client.fullName}</div>
            </div>
            <div>
              <div style={s.label}>Passport Number</div>
              <div style={s.val}>{client.passportNumber}</div>
            </div>
            <div>
              <div style={s.label}>Nationality</div>
              <div style={s.val}>{client.nationality}</div>
            </div>
            <div>
              <div style={s.label}>Date of Birth</div>
              <div style={s.val}>{client.dateOfBirth}</div>
            </div>
            <div>
              <div style={s.label}>Phone</div>
              <div style={s.val}>{client.phone || '—'}</div>
            </div>
            <div>
              <div style={s.label}>Email</div>
              <div style={s.val}>{client.email || '—'}</div>
            </div>
          </div>

          {client.address && (
            <div style={{ borderTop: '1px solid rgba(255,255,255,0.04)', paddingTop: 12 }}>
              <div style={s.label}>Residential Address</div>
              <div style={{ color: 'var(--text-main)', fontSize: '0.9rem', marginTop: 4, lineHeight: 1.5 }}>{client.address}</div>
            </div>
          )}
        </div>

        {/* Right Column: Associated Visa Cases */}
        <div className="glass-card" style={{ padding: 0, overflow: 'hidden' }}>
          <div style={{ padding: '20px 24px', borderBottom: '1px solid var(--glass-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 600 }}>📁 Associated Visa Cases</h2>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', background: 'rgba(255,255,255,0.05)', padding: '4px 10px', borderRadius: 9999 }}>
              {client.cases.length} {client.cases.length === 1 ? 'case' : 'cases'} total
            </span>
          </div>

          {client.cases.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
              No visa cases registered for this client yet.
            </div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ background: 'rgba(255, 255, 255, 0.02)', borderBottom: '1px solid var(--glass-border)', color: 'var(--text-muted)', fontSize: '0.78rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  <th style={{ padding: '14px 20px' }}>Reference</th>
                  <th style={{ padding: '14px 20px' }}>Program / Type</th>
                  <th style={{ padding: '14px 20px' }}>Stage</th>
                  <th style={{ padding: '14px 20px' }}>Status</th>
                  <th style={{ padding: '14px 20px', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {client.cases.map((c) => (
                  <tr key={c.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.03)', transition: 'background 0.2s ease' }}>
                    <td style={{ padding: '14px 20px', fontWeight: 600, color: '#fff' }}>
                      {c.caseReference}
                    </td>
                    <td style={{ padding: '14px 20px', color: 'var(--text-main)' }}>
                      {c.visaProgramName || c.visaTypeName || '—'}
                    </td>
                    <td style={{ padding: '14px 20px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      {c.currentStageName || '—'}
                    </td>
                    <td style={{ padding: '14px 20px' }}>
                      <span className={`badge ${getStatusBadgeClass(c.status)}`}>
                        {c.status}
                      </span>
                    </td>
                    <td style={{ padding: '14px 20px', textAlign: 'right' }}>
                      <Link href={`/dashboard/cases/${c.id}`} className="btn-secondary" style={{ padding: '5px 10px', fontSize: '0.78rem', textDecoration: 'none' }}>
                        View Case →
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Edit Client Modal */}
      {showEditModal && (
        <div style={s.overlay} onClick={() => setShowEditModal(false)}>
          <div className="glass-card" style={s.modal} onClick={(e) => e.stopPropagation()}>
            <h3 style={{ color: '#fff', fontSize: '1.2rem', marginBottom: 20 }}>Edit Client Details</h3>

            {editError && (
              <div style={{ padding: '10px 14px', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: '8px', color: 'var(--color-danger)', fontSize: '0.85rem', marginBottom: 16 }}>
                {editError}
              </div>
            )}

            <form onSubmit={handleEditSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
                <div className="form-group" style={{ gridColumn: 'span 2' }}>
                  <label className="form-label">Full Name *</label>
                  <input
                    type="text"
                    className="form-input"
                    required
                    value={editForm.fullName}
                    onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Passport Number *</label>
                  <input
                    type="text"
                    className="form-input"
                    required
                    value={editForm.passportNumber}
                    onChange={(e) => setEditForm({ ...editForm, passportNumber: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Nationality *</label>
                  <NationalitySelect
                    value={editForm.nationality}
                    onChange={(val) => setEditForm({ ...editForm, nationality: val })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Date of Birth *</label>
                  <input
                    type="date"
                    className="form-input"
                    required
                    value={editForm.dateOfBirth}
                    onChange={(e) => setEditForm({ ...editForm, dateOfBirth: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Phone Number</label>
                  <PhoneInputWithCode
                    phoneCode={phoneCode}
                    phoneNumber={editForm.phone}
                    onCodeChange={setPhoneCode}
                    onNumberChange={(num) => setEditForm({ ...editForm, phone: num })}
                  />
                </div>

                <div className="form-group" style={{ gridColumn: 'span 2' }}>
                  <label className="form-label">Email Address</label>
                  <input
                    type="email"
                    className="form-input"
                    value={editForm.email}
                    onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                  />
                </div>

                <div className="form-group" style={{ gridColumn: 'span 2' }}>
                  <label className="form-label">Address</label>
                  <textarea
                    className="form-input"
                    style={{ minHeight: 60, resize: 'vertical' }}
                    value={editForm.address}
                    onChange={(e) => setEditForm({ ...editForm, address: e.target.value })}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 24 }}>
                <button type="button" className="btn-secondary" onClick={() => setShowEditModal(false)} disabled={saving}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Client Modal */}
      {showDeleteModal && (
        <div style={s.overlay} onClick={() => !deleting && setShowDeleteModal(false)}>
          <div className="glass-card" style={s.deleteModal} onClick={(e) => e.stopPropagation()}>
            <h3 style={{ color: 'var(--color-danger)', fontSize: '1.25rem', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              ⚠️ Confirm Client Deletion
            </h3>

            <p style={{ color: 'var(--text-main)', fontSize: '0.95rem', marginBottom: '12px', lineHeight: 1.5 }}>
              Are you sure you want to delete client <strong style={{ color: '#fff' }}>{client.fullName}</strong> (Passport: {client.passportNumber})?
            </p>

            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '20px', background: 'rgba(239, 68, 68, 0.08)', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '10px 14px', borderRadius: '8px', lineHeight: 1.4 }}>
              <strong>Warning:</strong> Deleting this client profile will also permanently remove all {client.cases.length} associated visa application(s), uploaded documents, stage histories, and case notes. This action cannot be undone.
            </p>

            {deleteError && (
              <div style={{ padding: '10px 14px', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: '8px', color: 'var(--color-danger)', fontSize: '0.85rem', marginBottom: 16 }}>
                {deleteError}
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
              <button type="button" className="btn-secondary" onClick={() => setShowDeleteModal(false)} disabled={deleting}>
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

const s: Record<string, React.CSSProperties> = {
  label: {
    fontSize: '0.72rem',
    color: 'var(--text-muted)',
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
    marginBottom: 4,
  },
  val: {
    color: '#fff',
    fontSize: '0.9rem',
    fontWeight: 500,
  },
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
    maxWidth: 540,
    padding: 32,
    borderRadius: 20,
  },
  deleteModal: {
    width: '100%',
    maxWidth: 500,
    padding: 28,
    borderRadius: 20,
  },
};

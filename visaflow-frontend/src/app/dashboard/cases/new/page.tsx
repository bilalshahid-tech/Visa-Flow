'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch } from '@/services/api';

import { NationalitySelect, PhoneInputWithCode, combinePhone } from '@/components/ClientFormInputs';

interface Client { id: string; fullName: string; passportNumber: string; nationality: string; }
interface Country { id: string; name: string; isoCode: string; }
interface VisaProgram { id: string; name: string; description: string; countryId: string; countryName: string; categoryName: string; hasJobRoleCondition: boolean; }

const JOB_ROLE_OPTIONS = [
  { value: 'TOURISM',      label: '🏨 Tourism & Hospitality' },
  { value: 'SERVICES',     label: '🛎 Services' },
  { value: 'CONSTRUCTION', label: '🏗 Construction' },
  { value: 'HEALTHCARE',   label: '🏥 Healthcare' },
  { value: 'TECHNOLOGY',   label: '💻 Technology & IT' },
  { value: 'OTHER',        label: '📦 Other' },
];

export default function NewCasePage() {
  const router = useRouter();
  const [step, setStep] = useState<1 | 2>(1);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Step 1 – client
  const [clientSearch, setClientSearch] = useState('');
  const [clientResults, setClientResults] = useState<Client[]>([]);
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);
  const [mode, setMode] = useState<'search' | 'new'>('search');
  const [phoneCode, setPhoneCode] = useState('+92');
  const [nc, setNc] = useState({ fullName: '', passportNumber: '', nationality: 'Pakistani', dateOfBirth: '', phone: '', email: '', address: '' });

  // Step 2 – country → program → job role
  const [countries, setCountries] = useState<Country[]>([]);
  const [programs, setPrograms] = useState<VisaProgram[]>([]);
  const [selectedCountryId, setSelectedCountryId] = useState('');
  const [selectedProgram, setSelectedProgram] = useState<VisaProgram | null>(null);
  const [jobRoleCategory, setJobRoleCategory] = useState('');
  const [loadingPrograms, setLoadingPrograms] = useState(false);

  // ── Client search debounce
  useEffect(() => {
    const t = setTimeout(async () => {
      if (!clientSearch.trim()) { setClientResults([]); return; }
      try {
        const res = await apiFetch<any>(`/clients?search=${encodeURIComponent(clientSearch)}&size=10`);
        setClientResults(res.content || []);
      } catch { setClientResults([]); }
    }, 300);
    return () => clearTimeout(t);
  }, [clientSearch]);

  // ── Load countries on entering step 2
  useEffect(() => {
    if (step !== 2) return;
    apiFetch<Country[]>('/countries').then(setCountries).catch(() => setCountries([]));
  }, [step]);

  // ── Load programs when country selected
  useEffect(() => {
    if (!selectedCountryId) { setPrograms([]); setSelectedProgram(null); return; }
    setLoadingPrograms(true);
    apiFetch<VisaProgram[]>(`/visa-programs?countryId=${selectedCountryId}`)
      .then(p => { setPrograms(p); setSelectedProgram(null); })
      .catch(() => setPrograms([]))
      .finally(() => setLoadingPrograms(false));
  }, [selectedCountryId]);

  const createNewClient = async (): Promise<Client> => {
    const payload = {
      fullName: nc.fullName.trim() || 'New Applicant',
      passportNumber: nc.passportNumber.trim() || 'PASS-' + Math.floor(100000 + Math.random() * 900000),
      nationality: nc.nationality.trim() || 'Pakistani',
      dateOfBirth: nc.dateOfBirth || '1995-01-01',
      phone: combinePhone(phoneCode, nc.phone) || null,
      email: nc.email.trim() || null,
      address: nc.address.trim() || null,
    };
    return apiFetch<Client>('/clients', { method: 'POST', bodyData: payload });
  };

  const handleSubmit = async () => {
    if (!selectedProgram) { setError('Please select a visa program.'); return; }
    if (selectedProgram.hasJobRoleCondition && !jobRoleCategory) {
      setError('Please select a Job Role Category for this program.'); return;
    }
    setLoading(true); setError('');
    try {
      let client = selectedClient;
      if (!client) {
        if (mode === 'new') { client = await createNewClient(); }
        else { setError('Please select a client.'); setLoading(false); return; }
      }
      const caseRes = await apiFetch<any>('/cases', {
        method: 'POST',
        bodyData: {
          clientId: client!.id,
          visaProgramId: selectedProgram.id,
          jobRoleCategory: jobRoleCategory || null,
        }
      });
      router.push(`/dashboard/cases/${caseRes.id}`);
    } catch (e: any) {
      setError(e.message || 'Failed to register case. Please check that the backend is running.');
    } finally { setLoading(false); }
  };

  return (
    <div style={s.page}>
      <div style={s.header}>
        <button className="btn-secondary" style={s.backBtn} onClick={() => router.back()}>← Back</button>
        <h1 style={s.pageTitle}>Register New Case</h1>
      </div>

      {/* Step indicator */}
      <div style={s.steps}>
        {['Client Details', 'Visa Program & Stage Setup'].map((label, i) => (
          <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ ...s.stepDot, background: step > i + 1 ? 'var(--color-success)' : step === i + 1 ? 'var(--primary)' : 'rgba(255,255,255,0.1)' }}>
              {step > i + 1 ? '✓' : i + 1}
            </div>
            <span style={{ color: step === i + 1 ? '#fff' : 'var(--text-dark)', fontSize: '0.9rem', fontWeight: step === i + 1 ? 600 : 400 }}>{label}</span>
            {i < 1 && <div style={s.stepLine} />}
          </div>
        ))}
      </div>

      {error && <div style={s.errorAlert}>{error}</div>}

      {/* ── Step 1: Client ── */}
      {step === 1 && (
        <div className="glass-card" style={s.card}>
          <h2 style={s.sectionTitle}>Step 1 — Select or Register Client</h2>
          <div style={s.tabRow}>
            {(['search', 'new'] as const).map(m => (
              <button key={m} className={mode === m ? 'btn-primary' : 'btn-secondary'} style={s.tabBtn}
                onClick={() => { setMode(m); setSelectedClient(null); }}>
                {m === 'search' ? '🔍 Search Existing Client' : '+ New Client'}
              </button>
            ))}
          </div>

          {mode === 'search' ? (
            <div>
              <div className="form-group">
                <label className="form-label">Search by name or passport number</label>
                <input className="form-input" placeholder="e.g. John Smith or AB1234567"
                  value={clientSearch} onChange={e => setClientSearch(e.target.value)} />
              </div>
              {clientResults.length > 0 && (
                <div style={s.resultsList}>
                  {clientResults.map(c => (
                    <div key={c.id} style={{ ...s.resultItem, background: selectedClient?.id === c.id ? 'rgba(99,102,241,0.15)' : 'rgba(255,255,255,0.02)' }}
                      onClick={() => setSelectedClient(c)}>
                      <div style={{ fontWeight: 600, color: '#fff' }}>{c.fullName}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Passport: {c.passportNumber} · {c.nationality}</div>
                    </div>
                  ))}
                </div>
              )}
              {selectedClient && (
                <div style={s.selectedBadge}>✅ Selected: <strong>{selectedClient.fullName}</strong> ({selectedClient.passportNumber})</div>
              )}
            </div>
          ) : (
            <div style={s.twoCol}>
              <div className="form-group">
                <label className="form-label">Full Name</label>
                <input className="form-input" type="text" placeholder="e.g. John Doe"
                  value={nc.fullName} onChange={e => setNc(prev => ({ ...prev, fullName: e.target.value }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Passport Number</label>
                <input className="form-input" type="text" placeholder="e.g. AB1234567"
                  value={nc.passportNumber} onChange={e => setNc(prev => ({ ...prev, passportNumber: e.target.value }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Nationality</label>
                <NationalitySelect value={nc.nationality} onChange={val => setNc(prev => ({ ...prev, nationality: val }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Date of Birth</label>
                <input className="form-input" type="date"
                  value={nc.dateOfBirth} onChange={e => setNc(prev => ({ ...prev, dateOfBirth: e.target.value }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Phone Number</label>
                <PhoneInputWithCode phoneCode={phoneCode} phoneNumber={nc.phone}
                  onCodeChange={setPhoneCode} onNumberChange={num => setNc(prev => ({ ...prev, phone: num }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input className="form-input" type="email" placeholder="name@example.com"
                  value={nc.email} onChange={e => setNc(prev => ({ ...prev, email: e.target.value }))} />
              </div>
              <div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label className="form-label">Address (Optional)</label>
                <textarea className="form-input" value={nc.address} onChange={e => setNc(p => ({ ...p, address: e.target.value }))} style={{ minHeight: 70 }} />
              </div>
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '24px' }}>
            <button className="btn-primary" onClick={() => { setError(''); setStep(2); }}
              disabled={mode === 'search' && !selectedClient}>
              Next: Select Visa Program →
            </button>
          </div>
        </div>
      )}

      {/* ── Step 2: Country → Visa Program → Job Role ── */}
      {step === 2 && (
        <div className="glass-card" style={s.card}>
          <h2 style={s.sectionTitle}>Step 2 — Visa Program & Stage Setup</h2>

          {/* Country selector */}
          <div className="form-group" style={{ marginBottom: 24 }}>
            <label className="form-label" style={{ marginBottom: 12 }}>1. Select Country</label>
            {countries.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Loading countries…</p>
            ) : (
              <div style={s.countryGrid}>
                {countries.map(c => (
                  <div key={c.id} onClick={() => setSelectedCountryId(c.id)}
                    style={{ ...s.countryCard, borderColor: selectedCountryId === c.id ? 'var(--primary)' : 'rgba(255,255,255,0.08)', background: selectedCountryId === c.id ? 'rgba(99,102,241,0.15)' : 'rgba(255,255,255,0.02)', boxShadow: selectedCountryId === c.id ? '0 0 16px rgba(99,102,241,0.3)' : 'none' }}>
                    <div style={{ fontSize: '1.6rem', marginBottom: 4 }}>🌍</div>
                    <div style={{ fontWeight: 600, color: selectedCountryId === c.id ? '#fff' : 'var(--text-main)', fontSize: '0.9rem' }}>{c.name}</div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-dark)', marginTop: 2 }}>{c.isoCode}</div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Visa Program selector */}
          {selectedCountryId && (
            <div className="form-group" style={{ marginBottom: 24 }}>
              <label className="form-label" style={{ marginBottom: 12 }}>2. Select Visa Program</label>
              {loadingPrograms ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>Loading programs…</p>
              ) : programs.length === 0 ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>No programs available for this country.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {programs.map(p => (
                    <div key={p.id} onClick={() => { setSelectedProgram(p); setJobRoleCategory(''); }}
                      style={{ ...s.programCard, borderColor: selectedProgram?.id === p.id ? 'var(--primary)' : 'rgba(255,255,255,0.08)', background: selectedProgram?.id === p.id ? 'rgba(99,102,241,0.12)' : 'rgba(255,255,255,0.02)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                          <div style={{ fontWeight: 700, color: selectedProgram?.id === p.id ? '#fff' : 'var(--text-main)', fontSize: '0.95rem' }}>{p.name}</div>
                          {p.description && <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 4 }}>{p.description}</div>}
                        </div>
                        <span style={{ fontSize: '0.72rem', color: 'var(--text-dark)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 6, padding: '2px 8px', flexShrink: 0, marginLeft: 12 }}>{p.categoryName}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Job Role Category — only if program requires it */}
          {selectedProgram?.hasJobRoleCondition && (
            <div className="form-group" style={{ marginBottom: 24, padding: '16px 20px', background: 'rgba(245,158,11,0.06)', borderRadius: 12, border: '1px solid rgba(245,158,11,0.2)' }}>
              <label className="form-label" style={{ marginBottom: 12 }}>
                3. Job Role Category <span style={{ color: 'var(--color-warning)', fontSize: '0.8rem' }}>— Required for this program</span>
              </label>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: 12 }}>
                This determines document requirements in later stages (e.g. Tourism roles require additional Skills Pass proof).
              </p>
              <div style={s.roleGrid}>
                {JOB_ROLE_OPTIONS.map(opt => (
                  <div key={opt.value} onClick={() => setJobRoleCategory(opt.value)}
                    style={{ ...s.roleCard, borderColor: jobRoleCategory === opt.value ? 'var(--color-warning)' : 'rgba(255,255,255,0.08)', background: jobRoleCategory === opt.value ? 'rgba(245,158,11,0.12)' : 'rgba(255,255,255,0.02)', color: jobRoleCategory === opt.value ? '#fff' : 'var(--text-main)' }}>
                    {opt.label}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Summary of selected program */}
          {selectedProgram && (
            <div style={{ padding: '12px 16px', background: 'rgba(16,185,129,0.06)', border: '1px solid rgba(16,185,129,0.2)', borderRadius: 10, marginBottom: 20, fontSize: '0.875rem', color: 'var(--color-success)' }}>
              ✅ <strong>{selectedProgram.name}</strong>{jobRoleCategory ? ` · ${JOB_ROLE_OPTIONS.find(o => o.value === jobRoleCategory)?.label}` : ''}
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '28px' }}>
            <button className="btn-secondary" onClick={() => setStep(1)}>← Back</button>
            <button className="btn-primary" onClick={handleSubmit}
              disabled={loading || !selectedProgram || (selectedProgram.hasJobRoleCondition && !jobRoleCategory)}>
              {loading ? 'Creating Case…' : '✓ Register Case'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

const s: Record<string, React.CSSProperties> = {
  page: { display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: 760, margin: '0 auto' },
  header: { display: 'flex', alignItems: 'center', gap: '16px' },
  backBtn: { padding: '8px 16px', fontSize: '0.85rem' },
  pageTitle: { fontSize: '1.6rem', color: '#fff' },
  steps: { display: 'flex', alignItems: 'center', gap: '12px', padding: '16px 24px', background: 'rgba(255,255,255,0.02)', borderRadius: 12, border: '1px solid rgba(255,255,255,0.05)' },
  stepDot: { width: 28, height: 28, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 700, color: '#fff', flexShrink: 0 },
  stepLine: { width: 48, height: 1, background: 'rgba(255,255,255,0.1)', flexShrink: 0 },
  errorAlert: { background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)', borderRadius: 10, color: 'var(--color-danger)', padding: '12px', fontSize: '0.875rem' },
  card: {},
  sectionTitle: { fontSize: '1.15rem', color: '#fff', marginBottom: '20px', fontWeight: 600 },
  tabRow: { display: 'flex', gap: '10px', marginBottom: '24px' },
  tabBtn: { fontSize: '0.875rem', padding: '8px 16px' },
  resultsList: { display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px' },
  resultItem: { padding: '12px 16px', borderRadius: 10, border: '1px solid rgba(255,255,255,0.05)', cursor: 'pointer', transition: 'all 0.2s ease' },
  selectedBadge: { marginTop: 12, padding: '10px 16px', background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.2)', borderRadius: 10, color: 'var(--color-success)', fontSize: '0.875rem' },
  twoCol: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 24px' },
  countryGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))', gap: 12 },
  countryCard: { padding: '16px 12px', borderRadius: 12, border: '1px solid', cursor: 'pointer', textAlign: 'center', transition: 'all 0.2s ease' },
  programCard: { padding: '14px 16px', borderRadius: 12, border: '1px solid', cursor: 'pointer', transition: 'all 0.2s ease' },
  roleGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: 10 },
  roleCard: { padding: '10px 14px', borderRadius: 10, border: '1px solid', cursor: 'pointer', fontSize: '0.875rem', fontWeight: 500, transition: 'all 0.2s ease', textAlign: 'center' },
};

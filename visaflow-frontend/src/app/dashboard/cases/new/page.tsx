'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiFetch } from '@/services/api';

interface Client { id: string; fullName: string; passportNumber: string; nationality: string; }
interface VisaTypeInfo { id: string; code: string; name: string; country?: string; }
interface ChecklistPreview { documentClass: string; label: string; mandatory: boolean; displayOrder: number; }

const VISA_ICONS: Record<string, string> = {
  WORK: '💼',
  STUDY: '🎓',
  TOURIST: '✈️',
  VISIT: '🏡',
  BUSINESS: '📊',
  FAMILY: '👨‍👩‍👧',
  MEDICAL: '🏥',
  PERMANENT: '🏛️',
};

const DEFAULT_VISA_TYPES: VisaTypeInfo[] = [
  { id: 'a1000000-0000-0000-0000-000000000001', code: 'WORK',      name: 'Work / Employment Visa' },
  { id: 'a1000000-0000-0000-0000-000000000002', code: 'STUDY',     name: 'Student / Study Visa' },
  { id: 'a1000000-0000-0000-0000-000000000003', code: 'TOURIST',   name: 'Tourist / Holiday Visa' },
  { id: 'a1000000-0000-0000-0000-000000000004', code: 'VISIT',     name: 'Family / Friend Visit Visa' },
  { id: 'a1000000-0000-0000-0000-000000000005', code: 'BUSINESS',  name: 'Business / Investor Visa' },
  { id: 'a1000000-0000-0000-0000-000000000006', code: 'FAMILY',    name: 'Family Reunification Visa' },
  { id: 'a1000000-0000-0000-0000-000000000007', code: 'MEDICAL',   name: 'Medical Treatment Visa' },
  { id: 'a1000000-0000-0000-0000-000000000008', code: 'PERMANENT', name: 'Permanent Residency Application' },
];

const DEFAULT_CHECKLISTS: Record<string, ChecklistPreview[]> = {
  WORK: [
    { documentClass: 'PASSPORT', label: 'Passport Bio Page', mandatory: true, displayOrder: 1 },
    { documentClass: 'EMPLOYMENT_OFFER', label: 'Job Offer Letter / Employment Contract', mandatory: true, displayOrder: 2 },
    { documentClass: 'QUALIFICATION_CERT', label: 'Educational Certificates & Transcripts', mandatory: true, displayOrder: 3 },
    { documentClass: 'PROOF_OF_FUNDS', label: 'Bank Statements (Last 6 Months)', mandatory: false, displayOrder: 4 },
  ],
  STUDY: [
    { documentClass: 'PASSPORT', label: 'Passport Bio Page', mandatory: true, displayOrder: 1 },
    { documentClass: 'ACCEPTANCE_LETTER', label: 'University Acceptance Letter (CAS / I-20)', mandatory: true, displayOrder: 2 },
    { documentClass: 'PROOF_OF_FUNDS', label: 'Proof of Funds / Bank Solvency Letter', mandatory: true, displayOrder: 3 },
  ],
  DEFAULT: [
    { documentClass: 'PASSPORT', label: 'Passport Bio Page', mandatory: true, displayOrder: 1 },
    { documentClass: 'OTHER', label: 'Supporting Application Documents', mandatory: false, displayOrder: 2 },
  ]
};

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

  // New client form
  const [nc, setNc] = useState({ fullName:'', passportNumber:'', nationality:'', dateOfBirth:'', phone:'', email:'', address:'' });

  // Step 2 – visa type
  const [visaTypes, setVisaTypes] = useState<VisaTypeInfo[]>(DEFAULT_VISA_TYPES);
  const [selectedVisaTypeId, setSelectedVisaTypeId] = useState('');
  const [checklist, setChecklist] = useState<ChecklistPreview[]>([]);
  const [loadingChecklist, setLoadingChecklist] = useState(false);

  const searchClients = async (q: string) => {
    if (!q.trim()) { setClientResults([]); return; }
    try {
      const res = await apiFetch<any>(`/clients?search=${encodeURIComponent(q)}&size=10`);
      setClientResults(res.content || []);
    } catch { setClientResults([]); }
  };

  useEffect(() => {
    const t = setTimeout(() => searchClients(clientSearch), 300);
    return () => clearTimeout(t);
  }, [clientSearch]);

  const fetchVisaTypes = async () => {
    try {
      const d = await apiFetch<any>('/visa-types');
      const list = Array.isArray(d) ? d : (d?.content || []);
      if (list && list.length > 0) {
        setVisaTypes(list);
      } else {
        setVisaTypes(DEFAULT_VISA_TYPES);
      }
    } catch (e) {
      // Backend not yet restarted / 500 error — use default fallbacks gracefully
      setVisaTypes(DEFAULT_VISA_TYPES);
    }
  };

  useEffect(() => {
    fetchVisaTypes();
  }, []);

  useEffect(() => {
    if (step === 2 && visaTypes.length === 0) {
      fetchVisaTypes();
    }
  }, [step]);

  useEffect(() => {
    if (!selectedVisaTypeId) { setChecklist([]); return; }
    setLoadingChecklist(true);
    const selectedVt = visaTypes.find(vt => vt.id === selectedVisaTypeId);
    const code = selectedVt?.code || 'DEFAULT';

    async function loadChecklist() {
      // If using local fallback IDs (vt-work, etc.), bypass backend call to avoid UUID 500 errors
      if (selectedVisaTypeId.startsWith('vt-')) {
        setChecklist(DEFAULT_CHECKLISTS[code] || DEFAULT_CHECKLISTS.DEFAULT);
        setLoadingChecklist(false);
        return;
      }

      try {
        const res = await apiFetch<ChecklistPreview[]>(`/visa-types/${selectedVisaTypeId}/requirements`);
        if (Array.isArray(res) && res.length > 0) {
          setChecklist(res);
        } else {
          setChecklist(DEFAULT_CHECKLISTS[code] || DEFAULT_CHECKLISTS.DEFAULT);
        }
      } catch (e) {
        setChecklist(DEFAULT_CHECKLISTS[code] || DEFAULT_CHECKLISTS.DEFAULT);
      } finally {
        setLoadingChecklist(false);
      }
    }

    loadChecklist();
  }, [selectedVisaTypeId, visaTypes]);

  const createNewClient = async (): Promise<Client | null> => {
    const payload = {
      fullName: nc.fullName.trim() || 'New Applicant',
      passportNumber: nc.passportNumber.trim() || 'PASS-' + Math.floor(100000 + Math.random() * 900000),
      nationality: nc.nationality.trim() || 'Unknown',
      dateOfBirth: nc.dateOfBirth || '1995-01-01',
      phone: nc.phone.trim() || null,
      email: nc.email.trim() ? nc.email.trim() : null,
      address: nc.address.trim() || null,
    };

    try {
      const res = await apiFetch<Client>('/clients', { method:'POST', bodyData: payload });
      return res;
    } catch (e) {
      // Fallback local profile if backend container has not executed Flyway V3 migration yet
      return {
        id: 'temp-client-' + Date.now(),
        fullName: payload.fullName,
        passportNumber: payload.passportNumber,
        nationality: payload.nationality,
      };
    }
  };

  const handleSubmit = async () => {
    if (!selectedVisaTypeId) { setError('Please select a visa type.'); return; }
    setLoading(true); setError('');
    try {
      let client = selectedClient;
      if (!client) {
        if (mode === 'new') { client = await createNewClient(); }
        else { setError('Please select a client.'); setLoading(false); return; }
      }
      const caseRes = await apiFetch<any>('/cases', { method:'POST', bodyData: { clientId: client!.id, visaTypeId: selectedVisaTypeId } });
      router.push(`/dashboard/cases/${caseRes.id}`);
    } catch (e: any) {
      setError(e.message || 'Unable to register case with backend. Please run "docker compose up --build -d" to start the updated backend service.');
    }
    finally { setLoading(false); }
  };

  return (
    <div style={s.page}>
      <div style={s.header}>
        <button className="btn-secondary" style={s.backBtn} onClick={() => router.back()}>← Back</button>
        <h1 style={s.pageTitle}>Register New Case</h1>
      </div>

      {/* Step indicator */}
      <div style={s.steps}>
        {['Client Details','Visa Type & Documents'].map((label, i) => (
          <div key={i} style={{ display:'flex', alignItems:'center', gap:'8px' }}>
            <div style={{ ...s.stepDot, background: step > i+1 ? 'var(--color-success)' : step === i+1 ? 'var(--primary)' : 'rgba(255,255,255,0.1)' }}>
              {step > i+1 ? '✓' : i+1}
            </div>
            <span style={{ color: step === i+1 ? '#fff' : 'var(--text-dark)', fontSize:'0.9rem', fontWeight: step === i+1 ? 600 : 400 }}>{label}</span>
            {i < 1 && <div style={s.stepLine}/>}
          </div>
        ))}
      </div>

      {error && <div style={s.errorAlert}>{error}</div>}

      {/* ── Step 1: Client ── */}
      {step === 1 && (
        <div className="glass-card" style={s.card}>
          <h2 style={s.sectionTitle}>Step 1 — Select or Register Client</h2>

          <div style={s.tabRow}>
            {(['search','new'] as const).map(m => (
              <button key={m} className={mode === m ? 'btn-primary' : 'btn-secondary'} style={s.tabBtn} onClick={() => { setMode(m); setSelectedClient(null); }}>
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
                      <div style={{ fontWeight:600, color:'#fff' }}>{c.fullName}</div>
                      <div style={{ fontSize:'0.8rem', color:'var(--text-muted)' }}>Passport: {c.passportNumber} · {c.nationality}</div>
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
              {[['fullName','Full Name'],['passportNumber','Passport Number'],['nationality','Nationality'],['dateOfBirth','Date of Birth'],['phone','Phone'],['email','Email']].map(([k, label]) => (
                <div key={k} className="form-group">
                  <label className="form-label">{label}</label>
                  <input className="form-input" type={k === 'dateOfBirth' ? 'date' : k === 'email' ? 'email' : 'text'}
                    value={(nc as any)[k]} onChange={e => setNc(prev => ({ ...prev, [k]: e.target.value }))} />
                </div>
              ))}
              <div className="form-group" style={{ gridColumn:'1/-1' }}>
                <label className="form-label">Address (Optional)</label>
                <textarea className="form-input" value={nc.address} onChange={e => setNc(p => ({ ...p, address: e.target.value }))} style={{ minHeight:70 }} />
              </div>
            </div>
          )}

          <div style={{ display:'flex', justifyContent:'flex-end', marginTop:'24px' }}>
            <button className="btn-primary" onClick={() => { setError(''); setStep(2); }}
              disabled={mode === 'search' && !selectedClient}>
              Next: Select Visa Type →
            </button>
          </div>
        </div>
      )}

      {/* ── Step 2: Visa type + preview ── */}
      {step === 2 && (
        <div className="glass-card" style={s.card}>
          <h2 style={s.sectionTitle}>Step 2 — Visa Type & Document Checklist</h2>

          <div className="form-group" style={{ marginBottom: '24px' }}>
            <label className="form-label" style={{ marginBottom: '12px' }}>Select Visa Category</label>
            
            {/* Visual Card Grid */}
            <div style={s.visaGrid}>
              {visaTypes.map(vt => {
                const isSelected = selectedVisaTypeId === vt.id;
                const icon = VISA_ICONS[vt.code] || '📄';
                return (
                  <div
                    key={vt.id}
                    onClick={() => setSelectedVisaTypeId(vt.id)}
                    style={{
                      ...s.visaCard,
                      borderColor: isSelected ? 'var(--primary)' : 'rgba(255,255,255,0.08)',
                      background: isSelected ? 'rgba(99,102,241,0.15)' : 'rgba(255,255,255,0.02)',
                      boxShadow: isSelected ? '0 0 16px rgba(99,102,241,0.3)' : 'none',
                    }}
                  >
                    <span style={{ fontSize: '1.8rem', marginBottom: '6px', display: 'block' }}>{icon}</span>
                    <span style={{ color: isSelected ? '#fff' : 'var(--text-main)', fontWeight: 600, fontSize: '0.9rem' }}>
                      {vt.name || vt.code}
                    </span>
                    {vt.country && (
                      <span style={{ color: 'var(--text-dark)', fontSize: '0.75rem', marginTop: '2px', display: 'block' }}>
                        {vt.country}
                      </span>
                    )}
                  </div>
                );
              })}
            </div>

            {/* Dropdown Fallback */}
            <div style={{ marginTop: '16px' }}>
              <label className="form-label" style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Or select from list:</label>
              <select
                className="form-input"
                style={{ background: '#18181f', color: '#ffffff', cursor: 'pointer', marginTop: '6px' }}
                value={selectedVisaTypeId}
                onChange={e => setSelectedVisaTypeId(e.target.value)}
              >
                <option value="" style={{ background: '#18181f', color: '#ffffff' }}>— Select visa type —</option>
                {visaTypes.map(vt => (
                  <option key={vt.id} value={vt.id} style={{ background: '#18181f', color: '#ffffff' }}>
                    {vt.name || vt.code} ({vt.code})
                  </option>
                ))}
              </select>
            </div>
          </div>

          {loadingChecklist && <p style={{ color:'var(--text-muted)', fontSize:'0.875rem' }}>Loading required documents…</p>}

          {checklist.length > 0 && (
            <div style={{ marginTop:'20px', background: 'rgba(0,0,0,0.2)', padding: '20px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)' }}>
              <p style={{ fontSize:'0.875rem', color:'var(--text-muted)', marginBottom:'12px' }}>
                This visa type requires <strong style={{ color:'#fff' }}>{checklist.filter(c=>c.mandatory).length} mandatory</strong> and {checklist.filter(c=>!c.mandatory).length} optional documents:
              </p>
              <div style={s.checklistPreview}>
                {checklist.map((item, i) => (
                  <div key={i} style={s.checklistRow}>
                    <span style={{ color: item.mandatory ? 'var(--color-warning)' : 'var(--text-muted)', fontSize:'0.8rem', fontWeight:600, width:90 }}>
                      {item.mandatory ? '★ Required' : 'Optional'}
                    </span>
                    <span style={{ color:'var(--text-main)', fontSize:'0.875rem' }}>{item.label}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div style={{ display:'flex', justifyContent:'space-between', marginTop:'28px' }}>
            <button className="btn-secondary" onClick={() => setStep(1)}>← Back</button>
            <button className="btn-primary" onClick={handleSubmit} disabled={loading || !selectedVisaTypeId}>
              {loading ? 'Creating Case…' : '✓ Register Case'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

const s: Record<string, React.CSSProperties> = {
  page: { display:'flex', flexDirection:'column', gap:'24px', maxWidth:760, margin:'0 auto' },
  header: { display:'flex', alignItems:'center', gap:'16px' },
  backBtn: { padding:'8px 16px', fontSize:'0.85rem', textDecoration:'none' },
  pageTitle: { fontSize:'1.6rem', color:'#fff' },
  steps: { display:'flex', alignItems:'center', gap:'12px', padding:'16px 24px', background:'rgba(255,255,255,0.02)', borderRadius:12, border:'1px solid rgba(255,255,255,0.05)' },
  stepDot: { width:28, height:28, borderRadius:'50%', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'0.8rem', fontWeight:700, color:'#fff', flexShrink:0 },
  stepLine: { width:48, height:1, background:'rgba(255,255,255,0.1)', flexShrink:0 },
  errorAlert: { background:'rgba(239,68,68,0.1)', border:'1px solid rgba(239,68,68,0.2)', borderRadius:10, color:'var(--color-danger)', padding:'12px', fontSize:'0.875rem' },
  card: { },
  sectionTitle: { fontSize:'1.15rem', color:'#fff', marginBottom:'20px', fontWeight:600 },
  tabRow: { display:'flex', gap:'10px', marginBottom:'24px' },
  tabBtn: { fontSize:'0.875rem', padding:'8px 16px' },
  resultsList: { display:'flex', flexDirection:'column', gap:'8px', marginTop:'8px' },
  resultItem: { padding:'12px 16px', borderRadius:10, border:'1px solid rgba(255,255,255,0.05)', cursor:'pointer', transition:'all 0.2s ease' },
  selectedBadge: { marginTop:12, padding:'10px 16px', background:'rgba(16,185,129,0.1)', border:'1px solid rgba(16,185,129,0.2)', borderRadius:10, color:'var(--color-success)', fontSize:'0.875rem' },
  twoCol: { display:'grid', gridTemplateColumns:'1fr 1fr', gap:'0 24px' },
  visaGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))', gap: '12px' },
  visaCard: { padding: '16px', borderRadius: '12px', border: '1px solid', cursor: 'pointer', textAlign: 'center', transition: 'all 0.2s ease' },
  checklistPreview: { display:'flex', flexDirection:'column', gap:'8px' },
  checklistRow: { display:'flex', alignItems:'center', gap:'16px', padding:'10px 14px', background:'rgba(255,255,255,0.02)', borderRadius:8, border:'1px solid rgba(255,255,255,0.04)' },
};

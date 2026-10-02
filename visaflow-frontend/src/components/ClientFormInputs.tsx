import React from 'react';
import { NATIONALITIES, PHONE_CODES, PhoneCodeOption } from '@/data/countriesData';

export function parsePhoneString(fullPhone: string | null | undefined): { code: string; number: string } {
  if (!fullPhone) return { code: '+92', number: '' };
  const trimmed = fullPhone.trim();
  if (!trimmed.startsWith('+')) {
    return { code: '+92', number: trimmed };
  }
  // Find matching dialing code (sort by longest code first e.g. +880 before +88)
  const sortedCodes = [...PHONE_CODES].sort((a, b) => b.code.length - a.code.length);
  const matched = sortedCodes.find((item) => trimmed.startsWith(item.code));
  if (matched) {
    const rest = trimmed.slice(matched.code.length).trim();
    return { code: matched.code, number: rest };
  }
  return { code: '+92', number: trimmed };
}

export function combinePhone(code: string, number: string): string {
  const numTrimmed = number.trim();
  if (!numTrimmed) return '';
  if (numTrimmed.startsWith('+')) return numTrimmed;
  return `${code} ${numTrimmed}`;
}

interface NationalitySelectProps {
  value: string;
  onChange: (val: string) => void;
  required?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

export function NationalitySelect({
  value,
  onChange,
  required = false,
  className = 'form-input',
  style,
}: NationalitySelectProps) {
  return (
    <select
      className={className}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      required={required}
      style={{
        cursor: 'pointer',
        appearance: 'none',
        backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2394a3b8'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E")`,
        backgroundRepeat: 'no-repeat',
        backgroundPosition: 'right 12px center',
        backgroundSize: '16px',
        paddingRight: '36px',
        ...style,
      }}
    >
      <option value="" disabled>
        -- Select Nationality --
      </option>
      {NATIONALITIES.map((nat) => (
        <option key={nat} value={nat}>
          {nat}
        </option>
      ))}
    </select>
  );
}

interface PhoneInputWithCodeProps {
  phoneCode: string;
  phoneNumber: string;
  onCodeChange: (code: string) => void;
  onNumberChange: (num: string) => void;
  style?: React.CSSProperties;
}

export function PhoneInputWithCode({
  phoneCode,
  phoneNumber,
  onCodeChange,
  onNumberChange,
  style,
}: PhoneInputWithCodeProps) {
  return (
    <div style={{ display: 'flex', gap: '8px', alignItems: 'center', ...style }}>
      {/* Phone book / Country dialing code dropdown */}
      <div style={{ position: 'relative', width: '140px', flexShrink: 0 }}>
        <select
          className="form-input"
          value={phoneCode || '+92'}
          onChange={(e) => onCodeChange(e.target.value)}
          title="Select Country Calling Code"
          style={{
            width: '100%',
            cursor: 'pointer',
            paddingRight: '26px',
            fontSize: '0.88rem',
            backgroundPosition: 'right 6px center',
            appearance: 'none',
            backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2394a3b8'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E")`,
            backgroundRepeat: 'no-repeat',
            backgroundSize: '14px',
          }}
        >
          {PHONE_CODES.map((p) => (
            <option key={`${p.iso}-${p.code}`} value={p.code}>
              {p.flag} {p.code} ({p.iso})
            </option>
          ))}
        </select>
      </div>

      {/* Phone Number Input */}
      <input
        type="tel"
        className="form-input"
        placeholder="300 1234567"
        value={phoneNumber}
        onChange={(e) => onNumberChange(e.target.value)}
        style={{ flex: 1 }}
      />
    </div>
  );
}

import React from 'react';
import './globals.css';

export const metadata = {
  title: 'VisaFlow - Visa Consultancy Modular Platform',
  description: 'High-performance modular enterprise platform for visa consultancy case management.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <main>{children}</main>
      </body>
    </html>
  );
}

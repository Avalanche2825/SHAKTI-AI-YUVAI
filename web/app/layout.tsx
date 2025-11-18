import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'SHAKTI AI - Women\'s Safety App | AI-Powered Protection',
  description: 'India\'s first AI-powered women\'s safety app with automatic threat detection, dual-camera recording, GPS tracking, and offline community alerts.',
  keywords: 'women safety, AI threat detection, emergency app, India, SHAKTI AI',
  openGraph: {
    title: 'SHAKTI AI - Women\'s Safety App',
    description: 'AI-powered women\'s safety with automatic threat detection',
    type: 'website',
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}

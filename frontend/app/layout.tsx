import type { Metadata, Viewport } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/lib/auth-context";
import { Toaster } from "@/components/ui/sonner";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const viewport: Viewport = {
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#ffffff" },
    { media: "(prefers-color-scheme: dark)", color: "#0a0a0a" },
  ],
};

export const metadata: Metadata = {
  title: {
    default: "UTE Phone Hub - Cửa hàng điện thoại chính hãng",
    template: "%s | UTE Phone Hub",
  },
  description: "Cửa hàng điện thoại trực tuyến chính hãng. Mua điện thoại Samsung, iPhone, Xiaomi với giá tốt nhất, giao hàng nhanh toàn quốc.",
  keywords: ["điện thoại", "smartphone", "iPhone", "Samsung", "Xiaomi", "mua điện thoại", "UTE Phone Hub"],
  authors: [{ name: "UTE Phone Hub" }],
  creator: "UTE Phone Hub",
  icons: {
    icon: [
      { url: "/favicon.ico", sizes: "any" },
      { url: "/icon.svg", type: "image/svg+xml" },
    ],
    apple: "/apple-touch-icon.png",
  },
  openGraph: {
    title: "UTE Phone Hub - Cửa hàng điện thoại chính hãng",
    description: "Cửa hàng điện thoại trực tuyến chính hãng với giá tốt nhất",
    url: "https://utephonehub.com",
    siteName: "UTE Phone Hub",
    locale: "vi_VN",
    type: "website",
  },
  robots: {
    index: true,
    follow: true,
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <AuthProvider>
          {children}
          <Toaster position="top-right" richColors closeButton />
        </AuthProvider>
      </body>
    </html>
  );
}

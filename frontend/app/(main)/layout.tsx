import { Header, Footer } from '@/components/common';
import ScrollToTopButton from '@/components/common/ScrollToTopButton';

/**
 * Layout for main public pages (homepage, products, cart, etc.)
 * Includes Header and Footer
 */
export default function MainLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      <Header />
      <main className="min-h-screen">
        {children}
      </main>
      <ScrollToTopButton />
      <Footer />
    </>
  );
}

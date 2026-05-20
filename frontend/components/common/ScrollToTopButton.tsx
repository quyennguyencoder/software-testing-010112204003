'use client';

import { useEffect, useState } from 'react';
import { ArrowUp } from 'lucide-react';

export default function ScrollToTopButton() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const onScroll = () => {
      setVisible(window.scrollY > 300);
    };

    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const handleClick = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <button
      aria-label="Lên đầu trang"
      title="Lên đầu trang"
      onClick={handleClick}
      className={`fixed bottom-6 right-6 z-50 flex h-12 w-12 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-lg transition-transform duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-foreground border border-transparent ${
        visible ? 'translate-y-0 opacity-100' : 'translate-y-6 opacity-0'
      } hover:translate-y-0 hover:opacity-100 hover:scale-105 hover:shadow-2xl hover:border-black active:border-transparent focus:border-transparent focus-visible:border-transparent`}
    >
      <ArrowUp className="h-5 w-5" />
    </button>
  );
}

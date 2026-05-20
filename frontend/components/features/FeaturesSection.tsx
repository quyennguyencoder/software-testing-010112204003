'use client';

import { Truck, Shield, Headphones, ShoppingCart } from 'lucide-react';

export function FeaturesSection() {
  const features = [
    { icon: Truck, text: 'Giao hàng miễn phí', sub: 'Đơn từ 500K', color: 'from-blue-500 to-cyan-500' },
    { icon: Shield, text: 'Bảo hành chính hãng', sub: '12 tháng', color: 'from-green-500 to-emerald-500' },
    { icon: Headphones, text: 'Hỗ trợ 24/7', sub: 'Hotline 1800.1234', color: 'from-blue-500 to-cyan-500' },
    { icon: ShoppingCart, text: 'Đổi trả dễ dàng', sub: 'Trong 30 ngày', color: 'from-orange-500 to-red-500' },
  ];

  return (
    <section className="bg-gradient-to-b from-background to-secondary/50 py-8 md:py-12">
      <div className="max-w-7xl mx-auto px-4">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
          {features.map((feature, index) => (
            <div
              key={index}
              className="group flex flex-col items-center gap-3 rounded-2xl border border-border/50 bg-card/50 backdrop-blur-sm p-5 shadow-sm transition-all duration-300 hover:-translate-y-2 hover:shadow-xl hover:border-primary/30"
            >
              <div className={`w-14 h-14 rounded-2xl bg-gradient-to-br ${feature.color} flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300`}>
                <feature.icon className="w-7 h-7 text-white" />
              </div>
              <div className="text-center">
                <p className="font-semibold text-foreground text-sm md:text-base mb-1">
                  {feature.text}
                </p>
                <p className="text-xs md:text-sm text-muted-foreground">{feature.sub}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

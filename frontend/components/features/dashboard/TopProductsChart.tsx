/**
 * TopProductsChart component - Display top selling products
 * Module M10.2 - View Dashboard
 */

'use client';

import { useMemo } from 'react';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ChartOptions
} from 'chart.js';
import { TopProduct } from '@/types';
import { Package } from 'lucide-react';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

interface TopProductsChartProps {
  data: TopProduct[];
}

export function TopProductsChart({ data }: TopProductsChartProps) {
  // Calculate total sold quantity and revenue with useMemo
  const stats = useMemo(() => ({
    totalSold: data.reduce((sum, product) => sum + product.totalSold, 0),
    totalRevenue: data.reduce((sum, product) => sum + product.revenue, 0),
  }), [data]);

  // Gradient colors from dark to light (top 1 is darkest)
 
  const backgroundColors = [
    'rgba(234, 179, 8, 0.9)',  // Top 1 - Base color dark
    'rgba(234, 179, 8, 0.75)', // Top 2
    'rgba(234, 179, 8, 0.6)',  // Top 3
    'rgba(234, 179, 8, 0.45)', // Top 4
    'rgba(234, 179, 8, 0.45)',  // Top 5 - Base color light
  ];

  const borderColors = [
    'rgba(234, 179, 8, 1)',
    'rgba(234, 179, 8, 0.9)',
    'rgba(234, 179, 8, 0.8)',
    'rgba(234, 179, 8, 0.7)',
    'rgba(234, 179, 8, 0.7)',
  ];

  const chartData = {
    labels: data.map(product => {
      // Truncate long product names
      const name = product.productName;
      return name.length > 30 ? name.substring(0, 30) + '...' : name;
    }),
    datasets: [
      {
        label: 'Số lượng đã bán',
        data: data.map(product => product.totalSold),
        backgroundColor: backgroundColors.slice(0, data.length),
        borderColor: borderColors.slice(0, data.length),
        borderWidth: 2,
        borderRadius: 6,
        barThickness: 40,
      },
    ],
  };

  const options: ChartOptions<'bar'> = {
    indexAxis: 'y', // Horizontal bar chart
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleColor: '#fff',
        bodyColor: '#fff',
        borderColor: 'rgba(255, 255, 255, 0.2)',
        borderWidth: 1,
        displayColors: false,
        callbacks: {
          title: (context) => {
            const index = context[0].dataIndex;
            return `#${index + 1} ${data[index].productName}`;
          },
          label: (context) => {
            const index = context.dataIndex;
            const product = data[index];
            return [
              `Đã bán: ${product.totalSold.toLocaleString('vi-VN')} sản phẩm`,
              `Doanh thu: ${product.revenue.toLocaleString('vi-VN')}đ`
            ];
          },
        },
      },
    },
    scales: {
      x: {
        beginAtZero: true,
        ticks: {
          color: '#9ca3af',
          font: {
            size: 12,
          },
          stepSize: 1,
          callback: function(value) {
            return Math.floor(Number(value));
          },
        },
        grid: {
          color: 'rgba(156, 163, 175, 0.1)',
        },
        title: {
          display: true,
          text: 'Số lượng đã bán',
          color: '#6b7280',
          font: {
            size: 13,
            weight: 'normal',
          },
        },
      },
      y: {
        ticks: {
          color: '#9ca3af',
          font: {
            size: 12,
          },
        },
        grid: {
          display: false,
        },
      },
    },
    interaction: {
      intersect: false,
      mode: 'index',
    },
  };

  return (
    <div className="bg-card rounded-xl border border-border p-4 md:p-6">
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center gap-2 mb-1">
          <Package className="w-5 h-5 text-primary" />
          <h2 className="text-lg font-semibold text-foreground">
            Top 5 Sản phẩm bán chạy
          </h2>
        </div>
        <p className="text-sm text-muted-foreground">
          Sản phẩm có số lượng bán nhiều nhất
        </p>
      </div>

      {/* Chart */}
      <div className="h-[320px] md:h-[380px]">
        <Bar data={chartData} options={options} />
      </div>

      {/* Products List with Details */}
      <div className="mt-6 pt-4 border-t border-border">
        <div className="space-y-3">
          {data.map((product, index) => (
            <div 
              key={product.productId}
              className="flex items-center justify-between p-3 rounded-lg bg-secondary/30 hover:bg-secondary/50 transition-colors"
            >
              {/* Rank & Name */}
              <div className="flex items-center gap-3 flex-1 min-w-0">
                <div 
                  className="flex items-center justify-center w-8 h-8 rounded-full text-xs font-bold text-white"
                  style={{ backgroundColor: borderColors[index] }}
                >
                  #{index + 1}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-foreground truncate">
                    {product.productName}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    ID: {product.productId}
                  </p>
                </div>
              </div>

              {/* Stats */}
              <div className="flex items-center gap-6 ml-4">
                <div className="text-right">
                  <p className="text-sm text-muted-foreground">Đã bán</p>
                  <p className="text-base font-bold text-foreground">
                    {product.totalSold.toLocaleString('vi-VN')}
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-muted-foreground">Doanh thu</p>
                  <p className="text-base font-bold text-primary">
                    {(product.revenue / 1000000).toFixed(1)}M
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div className="mt-4 pt-4 border-t border-border">
          <div className="grid grid-cols-2 gap-4">
            <div className="text-center p-3 rounded-lg bg-secondary/50">
              <p className="text-sm text-muted-foreground mb-1">Tổng số lượng bán</p>
              <p className="text-2xl font-bold text-foreground">
                {stats.totalSold.toLocaleString('vi-VN')}
              </p>
            </div>
            <div className="text-center p-3 rounded-lg bg-secondary/50">
              <p className="text-sm text-muted-foreground mb-1">Tổng doanh thu</p>
              <p className="text-2xl font-bold text-primary">
                {(stats.totalRevenue / 1000000).toFixed(1)}M
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

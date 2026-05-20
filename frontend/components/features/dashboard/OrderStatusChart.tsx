/**
 * OrderStatusChart component - Display order status distribution pie chart
 * Module M10.2 - View Dashboard
 */

'use client';

import { Pie } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  ChartOptions
} from 'chart.js';
import { OrderStatusChartData } from '@/types';

// Register Chart.js components
ChartJS.register(
  ArcElement,
  Tooltip,
  Legend
);

interface OrderStatusChartProps {
  data: OrderStatusChartData;
}

export function OrderStatusChart({ data }: OrderStatusChartProps) {
  // Màu sắc cho từng trạng thái đơn hàng
  const backgroundColors = [
    'rgb(251, 191, 36)',  // PENDING - Vàng (Amber)
    'rgb(59, 130, 246)',  // CONFIRMED - Xanh dương (Blue)
    'rgb(168, 85, 247)',  // SHIPPING - Tím (Purple)
    'rgb(34, 197, 94)',   // DELIVERED - Xanh lá (Green)
    'rgb(239, 68, 68)'    // CANCELLED - Đỏ (Red)
  ];

  const borderColors = [
    'rgba(251, 191, 36, 0.8)',
    'rgba(59, 130, 246, 0.8)',
    'rgba(168, 85, 247, 0.8)',
    'rgba(34, 197, 94, 0.8)',
    'rgba(239, 68, 68, 0.8)'
  ];

  const chartData = {
    labels: data.labels,
    datasets: [
      {
        label: 'Số đơn hàng',
        data: data.values,
        backgroundColor: backgroundColors,
        borderColor: borderColors,
        borderWidth: 2,
        hoverOffset: 8,
      },
    ],
  };

  const options: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'right',
        labels: {
          padding: 15,
          font: {
            size: 13,
            family: 'Inter, system-ui, sans-serif',
          },
          color: '#6b7280',
          usePointStyle: true,
          pointStyle: 'circle',
          generateLabels: (chart) => {
            const datasets = chart.data.datasets;
            const labels = chart.data.labels as string[];
            
            return labels.map((label, i) => ({
              text: `${label}: ${data.values[i]} (${data.percentages[i].toFixed(1)}%)`,
              fillStyle: backgroundColors[i],
              strokeStyle: borderColors[i],
              lineWidth: 2,
              hidden: false,
              index: i,
            }));
          },
        },
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleColor: '#fff',
        bodyColor: '#fff',
        borderColor: 'rgba(255, 255, 255, 0.2)',
        borderWidth: 1,
        displayColors: true,
        callbacks: {
          label: (context) => {
            const index = context.dataIndex;
            const value = data.values[index];
            const percentage = data.percentages[index];
            return [
              `Số lượng: ${value} đơn`,
              `Tỷ lệ: ${percentage.toFixed(1)}%`
            ];
          },
        },
      },
    },
    interaction: {
      intersect: false,
      mode: 'nearest',
    },
  };

  return (
    <div className="bg-card rounded-xl border border-border p-4 md:p-6">
      {/* Header */}
      <div className="mb-6">
        <h2 className="text-lg font-semibold text-foreground">
          Phân bố Đơn hàng theo Trạng thái
        </h2>
        <p className="text-sm text-muted-foreground mt-1">
          Tổng số: {data.totalOrders} đơn hàng
        </p>
      </div>

      {/* Chart */}
      <div className="h-[350px] md:h-[400px] flex items-center justify-center">
        <Pie data={chartData} options={options} />
      </div>

      {/* Summary Stats */}
      <div className="mt-6 pt-4 border-t border-border">
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          {data.labels.map((label, index) => (
            <div key={index} className="text-center p-3 rounded-lg bg-secondary/50">
              <div 
                className="w-3 h-3 rounded-full mx-auto mb-2" 
                style={{ backgroundColor: backgroundColors[index] }}
              />
              <p className="text-xs text-muted-foreground mb-1">{label}</p>
              <p className="text-lg font-bold text-foreground">{data.values[index]}</p>
              <p className="text-xs text-muted-foreground">{data.percentages[index].toFixed(1)}%</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

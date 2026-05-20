/**
 * RevenueChart component - Display revenue trend chart
 * Module M10.2 - View Dashboard
 */

'use client';

import { useState } from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  ChartOptions
} from 'chart.js';
import { Button } from '@/components/ui/button';
import { RevenueChartData, DashboardPeriod } from '@/types';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

interface RevenueChartProps {
  data: RevenueChartData;
  onPeriodChange: (period: DashboardPeriod) => void;
}

export function RevenueChart({ data, onPeriodChange }: RevenueChartProps) {
  const [selectedPeriod, setSelectedPeriod] = useState<DashboardPeriod>('THIRTY_DAYS');

  const chartData = {
    labels: data.labels,
    datasets: [
      {
        label: 'Doanh thu (VNĐ)',
        data: data.values,
        borderColor: 'rgb(59, 130, 246)',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        fill: true,
        tension: 0.4,
        borderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: 'rgb(59, 130, 246)',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
      },
    ],
  };

  const options: ChartOptions<'line'> = {
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
        borderColor: 'rgba(59, 130, 246, 0.5)',
        borderWidth: 1,
        displayColors: false,
        callbacks: {
          label: (context) => {
            const value = context.parsed.y;
            if (value === null || value === undefined) return '';
            return new Intl.NumberFormat('vi-VN', {
              style: 'currency',
              currency: 'VND'
            }).format(value);
          },
        },
      },
    },
    scales: {
      x: {
        grid: {
          display: false,
        },
        ticks: {
          color: '#6b7280',
          font: {
            size: 12,
          },
        },
      },
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.05)',
        },
        ticks: {
          color: '#6b7280',
          font: {
            size: 12,
          },
          callback: (value) => {
            const numValue = typeof value === 'number' ? value : 0;
            if (numValue >= 1000000) {
              return `${(numValue / 1000000).toFixed(1)}M`;
            }
            return `${(numValue / 1000).toFixed(0)}K`;
          },
        },
      },
    },
    interaction: {
      intersect: false,
      mode: 'index',
    },
  };

  const handlePeriodChange = (period: DashboardPeriod) => {
    setSelectedPeriod(period);
    onPeriodChange(period);
  };

  return (
    <div className="bg-card rounded-xl border border-border p-4 md:p-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-lg font-semibold text-foreground">
            Biểu đồ Doanh thu
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            Theo dõi xu hướng doanh thu theo thời gian
          </p>
        </div>
        
        {/* Period Selector */}
        <div className="flex gap-2">
          <Button
            variant={selectedPeriod === 'SEVEN_DAYS' ? 'default' : 'outline'}
            size="sm"
            onClick={() => handlePeriodChange('SEVEN_DAYS')}
            className="text-xs"
          >
            7 ngày
          </Button>
          <Button
            variant={selectedPeriod === 'THIRTY_DAYS' ? 'default' : 'outline'}
            size="sm"
            onClick={() => handlePeriodChange('THIRTY_DAYS')}
            className="text-xs"
          >
            30 ngày
          </Button>
          <Button
            variant={selectedPeriod === 'THREE_MONTHS' ? 'default' : 'outline'}
            size="sm"
            onClick={() => handlePeriodChange('THREE_MONTHS')}
            className="text-xs"
          >
            3 tháng
          </Button>
        </div>
      </div>

      {/* Chart */}
      <div className="h-[300px] md:h-[350px]">
        <Line data={chartData} options={options} />
      </div>

      {/* Summary Stats */}
      <div className="mt-6 pt-4 border-t border-border grid grid-cols-2 gap-4">
        <div>
          <p className="text-xs text-muted-foreground mb-1">Tổng doanh thu</p>
          <p className="text-lg md:text-xl font-bold text-foreground">
            {new Intl.NumberFormat('vi-VN', {
              style: 'currency',
              currency: 'VND',
              notation: 'compact',
              maximumFractionDigits: 1
            }).format(data.total)}
          </p>
        </div>
        <div>
          <p className="text-xs text-muted-foreground mb-1">Trung bình/ngày</p>
          <p className="text-lg md:text-xl font-bold text-foreground">
            {new Intl.NumberFormat('vi-VN', {
              style: 'currency',
              currency: 'VND',
              notation: 'compact',
              maximumFractionDigits: 1
            }).format(data.averagePerDay)}
          </p>
        </div>
      </div>
    </div>
  );
}

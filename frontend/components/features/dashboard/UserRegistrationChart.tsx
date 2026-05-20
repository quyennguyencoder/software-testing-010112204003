/**
 * UserRegistrationChart component - Display new user registration trends
 * Module M10.2 - View Dashboard
 */

'use client';

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
import { UserRegistrationChartData, RegistrationPeriod } from '@/types';
import { Button } from '@/components/ui/button';

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

interface UserRegistrationChartProps {
  data: UserRegistrationChartData;
  onPeriodChange?: (period: RegistrationPeriod) => void;
}

export function UserRegistrationChart({ data, onPeriodChange }: UserRegistrationChartProps) {
  const chartData = {
    labels: data.labels,
    datasets: [
      {
        label: 'Người dùng đăng ký mới',
        data: data.values,
        borderColor: 'rgb(34, 197, 94)',
        backgroundColor: 'rgba(34, 197, 94, 0.1)',
        borderWidth: 3,
        fill: true,
        tension: 0.4,
        pointRadius: 5,
        pointHoverRadius: 7,
        pointBackgroundColor: 'rgb(34, 197, 94)',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointHoverBackgroundColor: 'rgb(34, 197, 94)',
        pointHoverBorderColor: '#fff',
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
        borderColor: 'rgba(255, 255, 255, 0.2)',
        borderWidth: 1,
        displayColors: false,
        callbacks: {
          label: (context) => {
            return `Số người dùng: ${context.parsed.y}`;
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          stepSize: 1,
          color: '#9ca3af',
          font: {
            size: 12,
          },
          callback: function(value) {
            return Math.floor(Number(value));
          },
        },
        grid: {
          color: 'rgba(156, 163, 175, 0.1)',
        },
        title: {
          display: true,
          text: 'Số người dùng',
          color: '#6b7280',
          font: {
            size: 13,
            weight: 'normal',
          },
        },
      },
      x: {
        ticks: {
          color: '#9ca3af',
          font: {
            size: 12,
          },
          maxRotation: 45,
          minRotation: 0,
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

  // Calculate statistics
  const totalUsers = data.total; // Use 'total' from backend response
  const averagePerPeriod = data.values.length > 0 
    ? (totalUsers / data.values.length).toFixed(1)
    : '0';

  return (
    <div className="bg-card rounded-xl border border-border p-4 md:p-6">
      {/* Header with Period Selector */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <h2 className="text-lg font-semibold text-foreground">
            Người dùng đăng ký mới
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            {data.period === 'WEEKLY' ? 'Theo tuần' : 'Theo tháng'}
          </p>
        </div>
        
        {/* Period Buttons */}
        {onPeriodChange && (
          <div className="flex gap-2">
            <Button
              variant={data.period === 'WEEKLY' ? 'default' : 'outline'}
              size="sm"
              onClick={() => onPeriodChange('WEEKLY')}
              className="min-w-[80px]"
            >
              Tuần
            </Button>
            <Button
              variant={data.period === 'MONTHLY' ? 'default' : 'outline'}
              size="sm"
              onClick={() => onPeriodChange('MONTHLY')}
              className="min-w-[80px]"
            >
              Tháng
            </Button>
          </div>
        )}
      </div>

      {/* Chart */}
      <div className="h-[300px] md:h-[350px]">
        <Line data={chartData} options={options} />
      </div>

      {/* Summary Stats */}
      <div className="mt-6 pt-4 border-t border-border">
        <div className="grid grid-cols-2 gap-4">
          <div className="text-center p-4 rounded-lg bg-secondary/50">
            <p className="text-sm text-muted-foreground mb-1">Tổng số user mới</p>
            <p className="text-2xl font-bold text-green-600">{totalUsers}</p>
          </div>
          <div className="text-center p-4 rounded-lg bg-secondary/50">
            <p className="text-sm text-muted-foreground mb-1">
              Trung bình/{data.period === 'WEEKLY' ? 'tuần' : 'tháng'}
            </p>
            <p className="text-2xl font-bold text-foreground">{averagePerPeriod}</p>
          </div>
        </div>
      </div>
    </div>
  );
}

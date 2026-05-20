export const sampleDashboard = {
  metrics: [
    { id: 'orders', title: 'Tổng đơn', value: '1,254', trend: 4.5, spark: [10,18,12,22,14] },
    { id: 'revenue', title: 'Doanh thu', value: '₫ 1,245,000,000', trend: 2.1, spark: [20,30,22,40,36] },
    { id: 'aov', title: 'Giá trị TB giỏ hàng', value: '₫ 993,000', trend: -1.2, spark: [15,14,16,13,12] },
    { id: 'abandon', title: 'Tỉ lệ bỏ giỏ', value: '34%', trend: 0.5, spark: [30,32,33,34,34] }
  ],
  topProduct: 'Điện thoại UTE X Pro',
  orders: Array.from({length:34}).map((_,i)=>({
    id: `ORD-${1000+i}`,
    customer: { name: `Khách ${i+1}`, email: `user${i+1}@mail.test` },
    items: [{ sku: 'SP-01', name: 'Sản phẩm A', qty: (i%3)+1, price: 1200000 }],
    total: ((i%3)+1)*1200000,
    status: i%4===0? 'COMPLETED' : i%4===1? 'PENDING' : 'CANCELLED',
    createdAt: new Date(Date.now() - i*1000*60*60*24).toISOString().slice(0,10)
  }))
}

package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class GeekTrust {

	public static class Order {
		private String id;
		private LocalDateTime time;
		private String stockSym;
		private String orderType;
		private double bidPrice;
		private Integer qty;

		public Order(String id, LocalDateTime time, String stockSym, String orderType, double bidPrice, Integer qty) {
			super();
			this.id = id;
			this.time = time;
			this.stockSym = stockSym;
			this.orderType = orderType;
			this.bidPrice = bidPrice;
			this.qty = qty;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public LocalDateTime getTime() {
			return time;
		}

		public void setTime(LocalDateTime time) {
			this.time = time;
		}

		public String getStockSym() {
			return stockSym;
		}

		public void setStockSym(String stockSym) {
			this.stockSym = stockSym;
		}

		public String getOrderType() {
			return orderType;
		}

		public void setOrderType(String orderType) {
			this.orderType = orderType;
		}

		public double getBidPrice() {
			return bidPrice;
		}

		public void setBidPrice(double bidPrice) {
			this.bidPrice = bidPrice;
		}

		public Integer getQty() {
			return qty;
		}

		public void setQty(Integer qty) {
			this.qty = qty;
		}
	}

	public static void main(String[] args) throws IOException {

		Queue<Order> orders = new LinkedList<GeekTrust.Order>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(args[0]));

		String curLine;
		while ((curLine = bufferedReader.readLine()) != null) {
			String[] inputArr = curLine.split("\\s+");
			Order o = new Order(inputArr[0], LocalDate.now().atTime(LocalTime.parse(inputArr[1])), inputArr[2],
					inputArr[3], Double.valueOf(inputArr[4]), Integer.valueOf(inputArr[5]));
			orders.offer(o);
		}
		bufferedReader.close();
		new OrderMatchingService().printTransactions(orders);

	}

	public static class OrderMatchingService {
		
		private Map<String, PriorityQueue<Order>[]> stockToPqMap = new HashMap<String, PriorityQueue<Order>[]>();

		private PriorityQueue<Order>[] getPqsForStock(String stockSymbol) {
			if (stockToPqMap.get(stockSymbol) == null) {
				PriorityQueue<Order> sellPq = new PriorityQueue<GeekTrust.Order>(
						Comparator.comparingDouble(Order::getBidPrice).thenComparing(Order::getTime));
				PriorityQueue<Order> buyPq = new PriorityQueue<GeekTrust.Order>(
						Comparator.comparingDouble(Order::getBidPrice).reversed().thenComparing(Order::getTime));
				stockToPqMap.put(stockSymbol, new PriorityQueue[] { sellPq, buyPq });
			}
			return stockToPqMap.get(stockSymbol);
		}

		public void printTransactions(Queue<Order> orders) {
			while (!orders.isEmpty()) {
				Order o = orders.poll();
				PriorityQueue<Order> sellPq = getPqsForStock(o.getStockSym())[0];
				PriorityQueue<Order> buyPq = getPqsForStock(o.getStockSym())[1];
				if ("sell".equals(o.getOrderType())) {
					handleSellOrder(o, sellPq, buyPq);
				} else {
					handleBuyOrder(o, sellPq, buyPq);
				}
			}
		}

		private void handleBuyOrder(Order o, PriorityQueue<Order> sellPq, PriorityQueue<Order> buyPq) {
			if (sellPq.isEmpty()) {
				buyPq.offer(o);
			} else {
				Order topSell = sellPq.peek();
				while (topSell != null && topSell.getBidPrice() <= o.getBidPrice()) {
					topSell = sellPq.poll();
					int diff = o.getQty() - topSell.getQty();
					System.out.println(o.getId() + " " + topSell.getBidPrice() + " "
							+ Math.min(o.getQty(), topSell.getQty()) + " " + topSell.getId());
					if (diff > 0) {
						o.setQty(Math.abs(diff));
					} else if (diff == 0) {
						o.setQty(0);
						break;
					} else {
						o.setQty(0);
						topSell.setQty(Math.abs(diff));
						sellPq.offer(topSell);
						break;
					}
					topSell = sellPq.peek();
				}
				if (o.getQty() != 0) {
					buyPq.offer(o);
				}
			}
		}

		private void handleSellOrder(Order o, PriorityQueue<Order> sellPq, PriorityQueue<Order> buyPq) {
			if (buyPq.isEmpty()) {
				sellPq.offer(o);
			} else {
				Order topBuy = buyPq.peek();
				while (topBuy != null && topBuy.getBidPrice() >= o.getBidPrice()) {
					topBuy = buyPq.poll();
					int diff = topBuy.getQty() - o.getQty();
					System.out.println(topBuy.getId() + " " + o.getBidPrice() + " "
							+ Math.min(topBuy.getQty(), o.getQty()) + " " + o.getId());
					if (diff > 0) {
						o.setQty(0);
						topBuy.setQty(Math.abs(diff));
						buyPq.offer(topBuy);
						break;
					} else if (diff == 0) {
						o.setQty(0);
						break;
					} else {
						o.setQty(Math.abs(diff));
					}
					topBuy = buyPq.peek();
				}
				if (o.getQty() != 0) {
					sellPq.offer(o);
				}
			}
		}
	}

}

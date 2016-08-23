package cn.tf.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import cn.tf.bean.OrderItem;
import cn.tf.bean.Orders;
import cn.tf.dao.OrderDao;
import cn.tf.entities.Order;
import cn.tf.entities.Shopping;
import cn.tf.entities.UserInfo;
import cn.tf.utils.C3P0Util;
import cn.tf.utils.DBHelper;

public class OrderDaoImpl implements OrderDao {

	
	
	private QueryRunner qr=new QueryRunner(C3P0Util.getDataSource());
	
	//保存订单
	@Override
	public void save(Orders order) {
		
		try {
			qr.update("insert into orders (ordernum,price,nums,status,usid,stime) values (?,?,?,?,?,sysdate ) ", 
					order.getOrdernum(),order.getPrice(),order.getNumber(),order.getStatus(),
					order.getUserInfo()==null?null:order.getUserInfo().getUsid());
			List<OrderItem> items = order.getItems();
			for(OrderItem item:items){
				qr.update("insert into orderitems (id,nums,price,ordernum,gid) values (?,?,?,?,?)", 
						item.getId(),item.getNumber(),item.getPrice(),order.getOrdernum(),item.getGoods()==null?null:item.getGoods().getGid());
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Orders findByNum(String ordernum) {
		try {
			Orders order = qr.query("select * from orders where ordernum=?", new BeanHandler<Orders>(Orders.class), ordernum);
			if(order!=null){
				UserInfo userInfo = qr.query("select * from userInfo where usid=(select usid from orders where ordernum=?)", new BeanHandler<UserInfo>(UserInfo.class), ordernum);
				order.setUserInfo(userInfo);
			}
			return order;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	public void update(Orders order) {
		try {
			qr.update("update orders set price=?,nums=?,status=? where ordernum=?", order.getPrice(),order.getNumber(),order.getStatus(),order.getOrdernum());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	
	//查找订单
	@Override
	public List<Order> find(Integer spid,Integer rid,Integer pageNo, Integer pageSize) {

		DBHelper db=new DBHelper();
		List<Object>  params=new ArrayList<Object>();
		
		

		String sql=null;
		if(rid==1002 || rid==1003){
			if(pageNo==null){
				sql="  select   o.usid,g.gname,s.sname,o.ordernum,oi.price,oi.nums,o.status,g.pic ,extract(year from o.stime) year, extract(month from o.stime) month , extract(day from o.stime) day " 
						+" from orders   o    join  orderitems oi   on o.ordernum=oi.ordernum   "
						+" join  goods g  on  oi.gid=g.gid    join   shopping s   on s.spid=g.spid  "
						+"  group by  o.usid,g.gname,s.sname,o.ordernum,oi.price,oi.nums,o.status,g.pic ,extract(year from o.stime) , extract(month from o.stime) , extract(day from o.stime)  having 1= 1 ";
			}else{
				
				sql="select * from(select A.*,rownum  rn from (  select   o.usid,g.gname,s.sname,o.ordernum,oi.price,oi.nums,o.status,g.pic ,extract(year from o.stime) year, extract(month from o.stime) month , extract(day from o.stime) day  "
						+" from orders   o    join  orderitems oi   on o.ordernum=oi.ordernum   join  goods g  on  oi.gid=g.gid    join   shopping s   on s.spid=g.spid  "
						+" group by  o.usid,g.gname,s.sname,o.ordernum,oi.price,oi.nums,o.status,g.pic ,extract(year from o.stime) , extract(month from o.stime), extract(day from o.stime)  having 1= 1  ) A  where rownum<=? ) where rn>? ";
				params.add(pageNo*pageSize);
				params.add((pageNo-1)*pageSize);
			}
		}else  if(rid==1024){
			if(pageNo==null){
				sql="  select   o.usid,g.gname,s.sname,o.ordernum,oi.price,oi.nums,o.status,g.pic   " 
						+" from orders   o    join  orderitems oi   on o.ordernum=oi.ordernum   "
						+" join  goods g  on  oi.gid=g.gid    join   shopping s   on s.spid=g.spid   and s.spid=? ";
				params.add(spid);
			}else{
				
				sql="select * from(select A.*,rownum  rn from (  select   o.usid,g.gname,s.sname,o.ordernum,oi.price,oi.nums,o.status,g.pic   "
						+" from orders   o    join  orderitems oi   on o.ordernum=oi.ordernum   join  goods g  on  oi.gid=g.gid    join   shopping s   on s.spid=g.spid    and s.spid=?   ) A  where rownum<=? ) where rn>? ";
				params.add(spid);
				params.add(pageNo*pageSize);
				params.add((pageNo-1)*pageSize);
			}
		}
		
		
		return db.find(sql, params,Order.class);
	}
}

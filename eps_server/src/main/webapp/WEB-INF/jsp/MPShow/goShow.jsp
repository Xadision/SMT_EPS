<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%> <!doctype html><html lang="en"><head><base href="/eps_server/"><meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no"><meta charset="UTF-8"><title>数据统计</title><link rel="stylesheet" href="static/css/base.css"><script src="static/js/jquery-3.1.1.min.js"></script><script src="static/js/Table_now_Js.js"></script></head><body><!--提示框--><div class="showWaiting" id="showWaiting"><span>数据加载中，请等待<i><img src="static/images/shalou.gif"></i></span></div><div class="wechat"><!--头部显示当天--> <span class="timeShow" id="timeShow">当天数据统计</span><!--表格头部显示--><table><thead class="wechat-thead"><tr><td>线别</td><td colspan="3">数据统计</td></tr></thead><tbody class="wechat-tbody" id="wechat_tbody"></tbody></table></div></body></html>
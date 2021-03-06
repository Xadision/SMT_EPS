package com.jimi.smt.eps.display.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jimi.smt.eps.display.app.Main;
import com.jimi.smt.eps.display.constant.BoardResetReson;
import com.jimi.smt.eps.display.constant.ClientDevice;
import com.jimi.smt.eps.display.constant.ControlResult;
import com.jimi.smt.eps.display.entity.SocketLog;
import com.jimi.smt.eps.display.entity.CenterLogin;
import com.jimi.smt.eps.display.entity.ProgramItemVisit;
import com.jimi.smt.eps.display.entity.ResultData;
import com.jimi.smt.eps.display.pack.BoardResetPackage;
import com.jimi.smt.eps.display.pack.BoardResetReplyPackage;
import com.jimi.smt.eps.display.util.HttpHelper;

import cc.darhao.dautils.api.BytesParser;
import cc.darhao.dautils.api.FieldUtil;
import cc.darhao.jiminal.callback.OnConnectedListener;
import cc.darhao.jiminal.callback.OnReplyPackageArrivedListener;
import cc.darhao.jiminal.core.AsyncCommunicator;
import cc.darhao.jiminal.core.BasePackage;
import cc.darhao.jiminal.core.PackageParser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import okhttp3.Call;
import okhttp3.Response;
import com.alibaba.fastjson.JSON;

public class DisplayController implements Initializable {

	private static final String PACKAGE_PATH = "com.jimi.smt.eps.display.pack";
	// 刷新表格数据线程的启动延时时间
	private static final Integer TIME_DELAY = 0;
	// 刷线表格数据线程的启动间隔时间
	private static final Integer TIME_PERIOD = 5000;
	// 默认结果
	private static final Integer DEFAULT_RESULT = 2;

	private static final boolean IS_NETWORK = true;
	// 定时器是否更新数据
	private static boolean isUpdate = false;
	// 不需要填充
	private static int senceWidth = 0;
	@FXML
	private Label lineLb;
	@FXML
	private Label operatorNameLb;
	@FXML
	private Label workOrderLb;
	@FXML
	private Label boardTybeLb;
	@FXML
	private Label lineseatNameLb;
	@FXML
	private Label scanLineseatNameLb;
	@FXML
	private Label materialNoNameLb;
	@FXML
	private Label scanMaterialNoNameLb;
	/// 需填充下拉框
	@FXML
	private ComboBox<String> lineCb;
	@FXML
	private ComboBox<String> workOrderCb;
	@FXML
	private ComboBox<String> boardTybeCb;
	// 需填充标签label
	@FXML
	private Label operatorLb;
	@FXML
	private Label lineseatLb;
	@FXML
	private Label scanLineseatLb;
	@FXML
	private Label materialNoLb;
	@FXML
	private Label scanMaterialNoLb;
	@FXML
	private Label typeLb;
	@FXML
	private Label resultLb;
	@FXML
	private Label versionLb;
	@FXML
	private Button resetBt;

	@FXML
	private TableView<ResultData> DataTv;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableColumn lineseatCl;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableColumn storeIssueResultCl;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableColumn feedResultCl;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableColumn changeResultCl;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableColumn checkResultCl;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableColumn checkAllResultCl;
	@SuppressWarnings("rawtypes")
	@FXML
	private TableColumn firstCheckAllResultCl;

	// http连接
	HttpHelper httpHelper = new HttpHelper();

	// 定时器
	private static Timer updateTimer = new Timer(true);
	// 表格数据
	private ObservableList<ResultData> tableLsit = null;
	// 日志记录
	private Logger logger = LogManager.getRootLogger();

	// 异步通讯器
	AsyncCommunicator asyncCommunicator = null;
	// 板子数量重置包
	BoardResetPackage boardResetPackage = new BoardResetPackage();

	private List<String> lines = null;
	// 返回的结果
	private String result;

	private List<ProgramItemVisit> programItemVisits;
	// 操作员
	private String operator;

	// 重置请求
	private static final String RESET_ACTION = "program/reset";
	// 选择当前工单请求
	private static final String SWITCH_ACTION = "program/switch";
	// 查询所有产线请求
	private static final String GET_LINE_ACTION = "line/selectAll";
	// 查询用户请求
	private static final String GET_USER_ACTION = "login/selectById";
	// 查询工单请求
	private static final String GET_WORKORDER_ACTION = "program/selectWorkingOrder";
	// 查询版面类型请求
	private static final String GET_BOARDTYPE_ACTION = "program/selectWorkingBoardType";
	// 查询操作员请求
	private static final String GET_OPERATOR_ACTION = "program/selectLastOperatorByProgram";
	// 查询工单操作请求
	private static final String GET_ITEMVISIT_ACTION = "program/selectItemVisitByProgram";
	
	private Map<String, String> map = new HashMap<>();

	public void initialize(URL arg0, ResourceBundle arg1) {
		initVersionLb();
		initlineCb();
		initDatatTV();
		resetBtListener();
		lineCbChangeListener();
		workOrderChangeListener();
		boardTypeChangeListener();
		// 定时任务：刷新表单
		timeTask();

	}

	private void initVersionLb() {
		versionLb.setText("V" + Main.getVersion() + " © 2018 几米物联技术有限公司  All rights reserved.");
	}

	/**
	 * 初始化线号选择框
	 * 
	 * @throws IOException
	 */
	public void initlineCb() {
		lines = JSON.parseArray(sendRequest(GET_LINE_ACTION, null), String.class);
		ObservableList<String> lineList = FXCollections.observableArrayList(lines);
		lineCb.getItems().clear();
		lineCb.setItems(lineList);
	}

	/**
	 * 初始化表格
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initDatatTV() {

		lineseatCl.setCellValueFactory(new PropertyValueFactory<ResultData, String>("lineseat"));
		storeIssueResultCl.setCellValueFactory(new PropertyValueFactory<ResultData, Integer>("storeIssueResult"));
		feedResultCl.setCellValueFactory(new PropertyValueFactory<ResultData, Integer>("feedResult"));
		changeResultCl.setCellValueFactory(new PropertyValueFactory<ResultData, Integer>("changeResult"));
		checkResultCl.setCellValueFactory(new PropertyValueFactory<ResultData, Integer>("checkResult"));
		checkAllResultCl.setCellValueFactory(new PropertyValueFactory<ResultData, Integer>("checkAllResult"));
		firstCheckAllResultCl.setCellValueFactory(new PropertyValueFactory<ResultData, Integer>("firstCheckAllResult"));
		lineseatCl.setCellFactory(new Callback<TableColumn, TableCell>() {
			public TableCell<ResultData, String> call(TableColumn pColumn) {
				return new TableCell<ResultData, String>() {

					@Override
					public void updateItem(String item, boolean empty) {

						super.updateItem(item, empty);
						this.setStyle("-fx-font-size: " + ((((senceWidth - 18) / 7 - 111) / 12) + 22) + "px;"
								+ "	-fx-alignment: center;" + "	-fx-font-family:'STXiHei';"
								+ "	-fx-font-weight: bold;");
						this.setText(item);
					}
				};
			}
		});
		initcell(storeIssueResultCl);
		initcell(feedResultCl);
		initcell(changeResultCl);
		initcell(checkResultCl);
		initcell(checkAllResultCl);
		initcell(firstCheckAllResultCl);
	}

	/**
	 * 初始化表格中某一列的每个单元格
	 * 
	 * @param tableColumn 列
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void initcell(TableColumn tableColumn) {

		tableColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
			public TableCell<ResultData, Integer> call(TableColumn pColumn) {
				return new TableCell<ResultData, Integer>() {

					@Override
					public void updateItem(Integer item, boolean empty) {

						super.updateItem(item, empty);
						this.setStyle("-fx-font-size:" + ((((senceWidth - 18) / 7 - 111) / 12) + 22) + "px;"
								+ "	-fx-alignment: center;" + "	-fx-font-family:'STXiHei';"
								+ "	-fx-font-weight: bold;");
						this.setText(null);
						if (!empty) {
							if (item.toString().equals("0")) {
								this.setText("×");
								this.setTextFill(Color.RED);
							} else if (item.toString().equals("1")) {
								this.setText("●");
								this.setTextFill(Color.GREEN);
							} else if (item.toString().equals("2")) {
								this.setText("○");
								this.setTextFill(Color.BLUE);
							} else if (item.toString().equals("3")) {
								this.setText("×");
								this.setTextFill(Color.ORANGE);
							} else if (item.toString().equals("4")) {
								this.setText("◎");
								this.setTextFill(Color.PURPLE);
							}
						}
					}
				};
			}
		});
	}

	/**
	 * 初始化定时器任务
	 */
	public void timeTask() {
		updateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				String line = lineCb.getSelectionModel().getSelectedItem() == null ? ""
						: lineCb.getSelectionModel().getSelectedItem().toString();
				String workOrder = workOrderCb.getSelectionModel().getSelectedItem() == null ? ""
						: workOrderCb.getSelectionModel().getSelectedItem().toString();
				String boardType = boardTybeCb.getSelectionModel().getSelectedItem() == null ? ""
						: boardTybeCb.getSelectionModel().getSelectedItem().toString();
				if (!line.equals("") && !workOrder.equals("") && !boardType.equals("")) {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							if (isUpdate) {
								updateText();
							}
						}
					});
				}
			}
		}, TIME_DELAY, TIME_PERIOD);
	}

	/**
	 * 线号选择框文本内容变更监听器
	 */
	public void lineCbChangeListener() {
		lineCb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() >= 0) {
					String line = lineCb.getItems().get(newValue.intValue());
					resetWorkOrderCb(line);
					map.clear();
					map.put("id", newValue.intValue() + 1 + "");
					String response = sendRequest(GET_USER_ACTION, map);
					CenterLogin login = null;
					if (!response.equals("")) {
						login = JSON.parseObject(response, CenterLogin.class);
						String remoteIp = login.getIp();
						int port = 23334;
						if (asyncCommunicator != null) {
							asyncCommunicator.close();
						}
						asyncCommunicator = new AsyncCommunicator(remoteIp, port, PACKAGE_PATH);
						// boardResetPackage.setLine(Line.values()[newValue.intValue()+1]);
						boardResetPackage.setLine(newValue.intValue() + 1 + "");
						boardResetPackage.setClientDevice(ClientDevice.PC);
						boardResetPackage.setBoardResetReson(BoardResetReson.WORK_ORDER_RESTART);
						logger.info(" IP: " + remoteIp + "  PORT: " + port + " LINE: " + line);
						clearText();
					} else {
						httpFail(GET_USER_ACTION, !IS_NETWORK, null);
					}
				}
			}
		});
	}

	/**
	 * 工单选择框文本内容变更监听器
	 */
	public void workOrderChangeListener() {

		workOrderCb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() >= 0) {
					String line = lineCb.getSelectionModel().getSelectedItem() == null ? ""
							: lineCb.getSelectionModel().getSelectedItem().toString();
					String workOrder = workOrderCb.getItems().get(newValue.intValue());
					if (!line.equals("") && !workOrder.equals("")) {
						map.clear();
						map.put("line", line);
						map.put("workOrder", workOrder);
						String response = sendRequest(GET_BOARDTYPE_ACTION, map);
						if (!response.equals("[]")) {
							List<String> boardTypes = JSON.parseArray(response, String.class);
							ObservableList<String> boardTybeList = FXCollections
									.observableArrayList(getBoardType(boardTypes));
							boardTybeCb.getItems().clear();
							boardTybeCb.setItems(boardTybeList);
							clearText();
						} else {
							httpFail(GET_BOARDTYPE_ACTION, !IS_NETWORK, null);
						}
					}
				}

			}
		});

	}

	/**
	 * 板面类型选择框文本内容变更监听器
	 */
	public void boardTypeChangeListener() {
		boardTybeCb.getSelectionModel();
		boardTybeCb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue != null && newValue.intValue() >= 0) {
					String line = lineCb.getSelectionModel().getSelectedItem() == null ? ""
							: lineCb.getSelectionModel().getSelectedItem().toString();
					String workOrder = workOrderCb.getSelectionModel().getSelectedItem() == null ? ""
							: workOrderCb.getSelectionModel().getSelectedItem().toString();
					String boardType = boardTybeCb.getItems().get(newValue.intValue());
					if (!line.equals("") && !workOrder.equals("") && boardType != null && !boardType.equals("")
							&& asyncCommunicator != null) {
						Integer boardTypeNo = getBoardTypeNo(boardType);
						setDisableCb(true);
						resetBt.setDisable(true);
						isUpdate = false;
						asyncCommunicator.connect(new OnConnectedListener() {
							// 发送工单生产数量重置包
							@Override
							public void onSucceed() {
								asyncCommunicator.send(boardResetPackage, new OnReplyPackageArrivedListener() {

									@Override
									public void onReplyPackageArrived(BasePackage r) {
										SocketLog sLog = createLogByPackage(boardResetPackage);
										logger.info("发送重置包：" + sLog.getData());
										if (r != null && r instanceof BoardResetReplyPackage) {
											BoardResetReplyPackage reply = (BoardResetReplyPackage) r;
											SocketLog rLog = createLogByPackage(reply);
											logger.info("接收重置包：" + rLog.getData());
											if (reply.getControlResult().equals(ControlResult.SUCCEED)) {
												// 发送选择工单请求
												map.clear();
												map.put("line", line);
												map.put("workOrder", workOrder);
												map.put("boardType", boardTypeNo.toString());
												String response = sendRequest(SWITCH_ACTION, map);
												if (response.equals("{\"result\":\"succeed\"}")) {
													Platform.runLater(new Runnable() {
														@Override
														public void run() {
															isUpdate = true;
															setDisableCb(false);
															resetBt.setDisable(false);
															updateText();
														}
													});
												} else {
													httpFail(SWITCH_ACTION, !IS_NETWORK, line);
												}
											} else {
												resetProductNbFail(!IS_NETWORK, line);
											}
										} else {
											resetProductNbFail(!IS_NETWORK, line);
										}
										if (asyncCommunicator != null) {
											asyncCommunicator.close();
										}
									}

									@Override
									public void onCatchIOException(IOException e) {
										resetProductNbFail(IS_NETWORK, line);
										e.printStackTrace();
										if (asyncCommunicator != null) {
											asyncCommunicator.close();
										}
									}
								});
							}

							@Override
							public void onFailed(IOException e) {
								resetProductNbFail(IS_NETWORK, line);
								if (asyncCommunicator != null) {
									asyncCommunicator.close();
								}
								e.printStackTrace();

							}
						});

					}
				}
			}
		});

	}

	/**
	 * 重置按钮监听器
	 */
	public void resetBtListener() {
		resetBt.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String boardType = boardTybeCb.getSelectionModel().getSelectedItem() == null ? ""
						: boardTybeCb.getSelectionModel().getSelectedItem().toString();
				if (boardType.equals("") || !isUpdate) {
					new Alert(AlertType.INFORMATION, "你还没有选定工单信息", ButtonType.OK).show();
				} else {
					Optional<ButtonType> optional = new Alert(AlertType.WARNING, "你确定要重置该工单的状态吗？\n这会初始化除发料以外的所有状态",
							ButtonType.YES, ButtonType.CANCEL).showAndWait();
					if (optional != null && optional.get().equals(ButtonType.YES)) {
						map.clear();
						String line = lineCb.getSelectionModel().getSelectedItem().toString();
						String workOrder = workOrderCb.getSelectionModel().getSelectedItem().toString();
						Integer boardTypeNo = getBoardTypeNo(boardType);
						map.put("line", line);
						map.put("workOrder", workOrder);
						map.put("boardType", boardTypeNo.toString());
						setDisableCb(true);
						resetBt.setDisable(true);
						String response = sendRequest(RESET_ACTION, map);
						if (response != null && response.equals("{\"result\":\"succeed\"}")) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									setDisableCb(false);
									resetBt.setDisable(false);
									updateText();
								}
							});
						} else {
							httpFail(RESET_ACTION, !IS_NETWORK, null);
						}
					}
				}
			}
		});
	}

	/**
	 * 重置工单生产数量失败
	 * 
	 * @param network 是否是网络原因
	 * @param line    重置失败的线号
	 */
	private void resetProductNbFail(boolean network, String line) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (network) {
					logger.error("重置工单的生产数目失败，IP地址：" + asyncCommunicator.getRemoteIp() + "connect失败，网络连接出错");
					new Alert(AlertType.ERROR, "重置工单生产数目失败，请检查你的网络连接", ButtonType.OK).show();
				} else {
					logger.error("重置工单的生产数目失败，服务器原因");
					new Alert(AlertType.ERROR, "重置工单生产数目失败，服务器原因", ButtonType.OK).show();
				}
				setDisableCb(false);
				resetBt.setDisable(false);
				resetWorkOrderCb(line);
			}
		});

	}

	/**
	 * 发送http请求失败
	 * 
	 * @param action    http请求类型
	 * @param isNetwork 是否是网络问题
	 * @param line      线号
	 */
	private void httpFail(String action, boolean isNetwork, String line) {
		if (action.equals(SWITCH_ACTION)) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (isNetwork) {
						logger.error("选定工单失败，switch请求失败，网络连接出错");
						new Alert(AlertType.ERROR, "选定工单失败，请检查你的网络连接", ButtonType.OK).show();
					} else {
						logger.error("选定工单失败，服务器内部错误");
						new Alert(AlertType.ERROR, "选定工单失败，服务器内部错误", ButtonType.OK).show();
					}
					setDisableCb(false);
					resetBt.setDisable(false);
					resetWorkOrderCb(line);
				}
			});
			return;
		}
		if (action.equals(RESET_ACTION)) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (isNetwork) {
						logger.error("重置工单失败，reset请求失败，网络连接出错");
						new Alert(AlertType.ERROR, "重置工单失败，请检查你的网络连接", ButtonType.OK).show();
					} else {
						logger.error("重置工单失败，服务器内部原因");
						new Alert(AlertType.ERROR, "重置工单失败，服务器内部错误", ButtonType.OK).show();
					}
					setDisableCb(false);
					resetBt.setDisable(false);
				}
			});
			return;
		}
		if (action.equals(GET_LINE_ACTION)) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (isNetwork) {
						logger.error("查询所有产线失败，select_all请求失败，网络连接出错");
						new Alert(AlertType.ERROR, "查询所有产线失败，请检查你的网络连接", ButtonType.OK).show();
					} else {
						logger.error("查询所有产线失败，服务器内部原因");
						new Alert(AlertType.ERROR, "查询所有产线失败，服务器内部错误", ButtonType.OK).show();
					}
					setDisableCb(false);
					resetBt.setDisable(false);
				}
			});
			return;
		}
		if (action.equals(GET_USER_ACTION)) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (isNetwork) {
						logger.error("查询登录对象失败，select_id请求失败，网络连接出错");
						new Alert(AlertType.ERROR, "查询登录对象失败，请检查你的网络连接", ButtonType.OK).show();
					} else {
						logger.error("查询登录对象失败，服务器内部原因");
						new Alert(AlertType.ERROR, "查询登录对象失败，服务器内部错误", ButtonType.OK).show();
					}
					setDisableCb(false);
					resetBt.setDisable(false);
				}
			});
			return;
		}
		if (action.equals(GET_WORKORDER_ACTION)) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (isNetwork) {
						logger.error("查询工单失败，select_workorder请求失败，网络连接出错");
						new Alert(AlertType.ERROR, "查询工单失败，请检查你的网络连接", ButtonType.OK).show();
					} else {
						logger.error("查询工单失败，服务器内部原因");
						new Alert(AlertType.ERROR, "查询工单失败，服务器内部错误", ButtonType.OK).show();
					}
					setDisableCb(false);
					resetBt.setDisable(false);
				}
			});
			return;
		}
		if (action.equals(GET_BOARDTYPE_ACTION)) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (isNetwork) {
						logger.error("查询版面类型失败，select_boardtype请求失败，网络连接出错");
						new Alert(AlertType.ERROR, "查询版面类型失败，请检查你的网络连接", ButtonType.OK).show();
					} else {
						logger.error("查询版面类型失败，服务器内部原因");
						new Alert(AlertType.ERROR, "查询版面类型失败，服务器内部错误", ButtonType.OK).show();
					}
					setDisableCb(false);
					resetBt.setDisable(false);
				}
			});
			return;
		}
		if (action.equals(GET_OPERATOR_ACTION)) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (isNetwork) {
						logger.error("查询操作员失败，select_operator请求失败，网络连接出错");
						new Alert(AlertType.ERROR, "查询操作员失败，请检查你的网络连接", ButtonType.OK).show();
					} else {
						logger.error("查询操作员失败，服务器内部原因");
						new Alert(AlertType.ERROR, "查询操作员失败，服务器内部错误", ButtonType.OK).show();
					}
					setDisableCb(false);
					resetBt.setDisable(false);
				}
			});
			return;
		}
		if (action.equals(GET_ITEMVISIT_ACTION)) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (isNetwork) {
						logger.error("查询工单操作失败，select_itemvisit请求失败，网络连接出错");
						new Alert(AlertType.ERROR, "查询工单操作失败，请检查你的网络连接", ButtonType.OK).show();
					} else {
						logger.error("查询工单操作失败，服务器内部原因");
						new Alert(AlertType.ERROR, "查询工单操作失败，服务器内部错误", ButtonType.OK).show();
					}
					setDisableCb(false);
					resetBt.setDisable(false);
				}
			});
		}
	}

	/**
	 * 设置下拉框的状态
	 * 
	 * @param status
	 */
	private void setDisableCb(boolean status) {
		lineCb.setDisable(status);
		workOrderCb.setDisable(status);
		boardTybeCb.setDisable(status);
	}

	/**
	 * 更新站位，扫描站位，物料号，扫描物料号，操作员，操作结果以及表格数据
	 * 
	 * @param program
	 * @throws IOException
	 */
	private synchronized void updateText() {

		String line = lineCb.getSelectionModel().getSelectedItem() == null ? ""
				: lineCb.getSelectionModel().getSelectedItem().toString();
		String workOrder = workOrderCb.getSelectionModel().getSelectedItem() == null ? ""
				: workOrderCb.getSelectionModel().getSelectedItem().toString();
		String boardType = boardTybeCb.getSelectionModel().getSelectedItem() == null ? ""
				: boardTybeCb.getSelectionModel().getSelectedItem().toString();
		if (!line.equals("") && !workOrder.equals("") && !boardType.equals("")) {
			Integer boardTypeNo = getBoardTypeNo(boardType);
			map.clear();
			map.put("line", line);
			map.put("workOrder", workOrder);
			map.put("boardType", boardTypeNo.toString());
			String itemVisits = sendRequest(GET_ITEMVISIT_ACTION, map);
			if (!itemVisits.equals("[]")) {
				programItemVisits = JSON.parseArray(itemVisits, ProgramItemVisit.class);
			} else {
				httpFail(GET_ITEMVISIT_ACTION, !IS_NETWORK, null);
			}
			String response = sendRequest(GET_OPERATOR_ACTION, map);
			operator = response.equals("") ? "unknown" : response;
			if (programItemVisits != null && programItemVisits.size() > 0) {
				ProgramItemVisit programItemVisit = programItemVisits.get(0);
				lineseatLb.setText(programItemVisit.getLineseat());
				scanLineseatLb.setText(programItemVisit.getScanLineseat());
				materialNoLb.setText(programItemVisit.getMaterialNo());
				scanMaterialNoLb.setText(programItemVisit.getScanMaterialNo());
				operatorLb.setText(operator);
				setTypeAndResult(programItemVisit);
				tableLsit = FXCollections.observableArrayList(programItemVisitToResultData(programItemVisits));
				DataTv.setItems(tableLsit);
			}
		}
	}

	/**
	 * 设置typeLb和resultLb文本值，更改操作结果
	 * 
	 * @param programItemVisit
	 */
	private void setTypeAndResult(ProgramItemVisit programItemVisit) {

		if (programItemVisit.getLastOperationType() != null) {
			switch (programItemVisit.getLastOperationType()) {
			case 0:
				typeLb.setText("上  料");
				showResult(programItemVisit.getFeedResult());
				break;
			case 1:
				typeLb.setText("换  料");
				showResult(programItemVisit.getChangeResult());
				break;
			case 2:
				typeLb.setText("核  料");
				showResult(programItemVisit.getCheckResult());
				break;
			case 3:
				typeLb.setText("全  检");
				showResult(programItemVisit.getCheckAllResult());
				break;
			case 4:
				typeLb.setText("发  料");
				showResult(programItemVisit.getStoreIssueResult());
				break;
			case 5:
				typeLb.setText("首  检");
				showResult(programItemVisit.getFirstCheckAllResult());
				break;
			default:
				showResult(DEFAULT_RESULT);
				break;
			}
		} else {
			showResult(DEFAULT_RESULT);
		}
	}

	/**
	 * 根据操作结果进行显示
	 * 
	 * @param result
	 */
	private void showResult(Integer result) {
		String style = "-fx-alignment:center;-fx-text-fill:white;-fx-font-family:'Microsoft YaHei';-fx-font-weight:bold;";
		String textsize = "";
		if (senceWidth < 800 && senceWidth > 600) {
			textsize = "-fx-font-size:140px;";
		} else if (senceWidth >= 800 && senceWidth < 1366) {
			textsize = "-fx-font-size:140px;";
		} else if (senceWidth >= 1366) {
			textsize = "-fx-font-size:140px;";
		}
		switch (result) {
		case 0:
			typeLb.setStyle(style + textsize + "-fx-background-color:red;");
			resultLb.setStyle(style + textsize + "-fx-background-color:red;");
			resultLb.setText("FAIL");
			break;
		case 1:
			typeLb.setStyle(style + textsize + "-fx-background-color:green;");
			resultLb.setStyle(style + textsize + "-fx-background-color:green;");
			resultLb.setText("PASS");
			break;
		case 2:
			typeLb.setStyle(style + textsize + "-fx-background-color:green;");
			resultLb.setStyle(style + textsize + "-fx-background-color:green;");
			typeLb.setText("操  作");
			resultLb.setText("结  果");
			break;
		case 3:
			typeLb.setStyle(style + textsize + "-fx-background-color:red;");
			resultLb.setStyle(style + textsize + "-fx-background-color:red;");
			resultLb.setText("已超时");
			break;
		case 4:
			typeLb.setStyle(style + textsize + "-fx-background-color:purple;");
			resultLb.setStyle(style + textsize + "-fx-background-color:purple;");
			typeLb.setText("已换料");
			resultLb.setText("请核料");
			break;
		default:
			break;
		}
	}

	/**
	 * 将ProgramItemVisit转化为可被表格识别的ResultData
	 * 
	 * @param programItemVisits
	 * @return
	 */
	private List<ResultData> programItemVisitToResultData(List<ProgramItemVisit> programItemVisits) {
		List<ResultData> resultDatas = new ArrayList<>();
		for (ProgramItemVisit programItemVisit : programItemVisits) {
			ResultData resultData = new ResultData();
			resultData.setLineseat(programItemVisit.getLineseat());
			resultData.setStoreIssueResult(programItemVisit.getStoreIssueResult());
			resultData.setFeedResult(programItemVisit.getFeedResult());
			resultData.setChangeResult(programItemVisit.getChangeResult());
			resultData.setCheckResult(programItemVisit.getCheckResult());
			resultData.setCheckAllResult(programItemVisit.getCheckAllResult());
			resultData.setFirstCheckAllResult(programItemVisit.getFirstCheckAllResult());
			resultDatas.add(resultData);
		}
		return resultDatas;
	}

	/**
	 * 清除文本框文本，表格数据
	 */
	private void clearText() {
		lineseatLb.setText("");
		scanLineseatLb.setText("");
		materialNoLb.setText("");
		scanMaterialNoLb.setText("");
		operatorLb.setText("");
		showResult(DEFAULT_RESULT);
		DataTv.setItems(null);
	}

	/**
	 * 清空板面类型下拉框，重置工单下拉框内容
	 * 
	 * @param line 线号
	 */
	private void resetWorkOrderCb(String line) {
		workOrderCb.getItems().clear();
		boardTybeCb.getItems().clear();
		map.clear();
		map.put("line", line);
		String response = sendRequest(GET_WORKORDER_ACTION, map);
		if (!response.equals("[]")) {
			List<String> workorders = JSON.parseArray(response, String.class);
			ObservableList<String> workOrderList = FXCollections.observableArrayList(workorders);
			workOrderCb.setItems(workOrderList);
		} else {
			httpFail(GET_WORKORDER_ACTION, !IS_NETWORK, line);
		}
	}

	/**
	 * 将板面类型数字转化为中文
	 * 
	 * @param boardTybes 板面类型数字字符数组
	 * @return
	 */
	private List<String> getBoardType(List<String> boardTybes) {
		List<String> boardTybeList = new ArrayList<>();
		for (String boardTybe : boardTybes) {
			if (boardTybe.equals("0")) {
				boardTybeList.add("默认");
			} else if (boardTybe.equals("1")) {
				boardTybeList.add("AB面");
			} else if (boardTybe.equals("2")) {
				boardTybeList.add("A面");
			} else if (boardTybe.equals("3")) {
				boardTybeList.add("B面");
			}
		}
		return boardTybeList;
	}

	/**
	 * 将板面类型中文转化为数字
	 * 
	 * @param boardType
	 * @return
	 */
	private Integer getBoardTypeNo(String boardType) {
		Integer boardTypeNo = null;
		if (boardType.equals("默认")) {
			boardTypeNo = 0;
		} else if (boardType.equals("AB面")) {
			boardTypeNo = 1;
		} else if (boardType.equals("A面")) {
			boardTypeNo = 2;
		} else if (boardType.equals("B面")) {
			boardTypeNo = 3;
		}
		return boardTypeNo;
	}

	/**
	 * 关闭程序事件
	 * 
	 * @param primaryStage
	 */
	public void closeWindow(Stage primaryStage) {
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				String line = lineCb.getSelectionModel().getSelectedItem() == null ? ""
						: lineCb.getSelectionModel().getSelectedItem().toString();
				if (!line.equals("") && asyncCommunicator != null) {
					asyncCommunicator.connect(new OnConnectedListener() {

						@Override
						public void onSucceed() {
							asyncCommunicator.send(boardResetPackage, new OnReplyPackageArrivedListener() {

								@Override
								public void onReplyPackageArrived(BasePackage r) {
									SocketLog sLog = createLogByPackage(boardResetPackage);
									logger.info("发送重置包：" + sLog.getData());
									if (r != null && r instanceof BoardResetReplyPackage) {
										BoardResetReplyPackage reply = (BoardResetReplyPackage) r;
										SocketLog rLog = createLogByPackage(reply);
										logger.info("接收重置包：" + rLog.getData());
										if (!reply.getControlResult().equals(ControlResult.SUCCEED)) {
											logger.error("关闭时重置工单的生产数目失败，取消工单失败，IP:" + asyncCommunicator.getRemoteIp()
													+ "服务器内部错误");
										} else {
											logger.info("关闭时重置生产数目成功");
										}
									} else {
										logger.error("关闭时重置工单的生产数目失败，取消工单失败，IP:" + asyncCommunicator.getRemoteIp()
												+ "服务器内部错误");
									}
									if (asyncCommunicator != null) {
										asyncCommunicator.close();
									}
									sendHttpCloseRequest(line);
								}

								@Override
								public void onCatchIOException(IOException exception) {
									logger.error(
											"关闭时重置工单的生产数目失败，取消工单失败，IP:" + asyncCommunicator.getRemoteIp() + "网络连接错误");
									if (asyncCommunicator != null) {
										asyncCommunicator.close();
									}
									sendHttpCloseRequest(line);
								}
							});
						}

						@Override
						public void onFailed(IOException e) {
							logger.error("关闭时重置工单的生产数目失败，取消工单失败，IP:" + asyncCommunicator.getRemoteIp() + "网络连接错误");
							if (asyncCommunicator != null) {
								asyncCommunicator.close();
							}
							sendHttpCloseRequest(line);
						}
					});
				}
			}
		});
	}

	/**
	 * http关闭请求取消工单
	 * 
	 * @param line
	 */
	private void sendHttpCloseRequest(String line) {
		map.clear();
		map.put("line", line);
		try {
			httpHelper.requestHttp(SWITCH_ACTION, map, new okhttp3.Callback() {
				@Override
				public void onResponse(Call call, Response response) throws IOException {
					if (response.body().string().equals("{\"result\":\"succeed\"}")) {
						logger.info("关闭时取消工单成功，线号：" + line);
					} else {
						logger.error("关闭时取消工单失败，服务器内部错误，线号：" + line);
					}
					System.exit(0);
				}

				@Override
				public void onFailure(Call call, IOException e) {
					logger.error("关闭时取消工单失败，网络连接出错，线号：" + line);
					System.exit(0);
				}
			});
		} catch (IOException httpe) {
			logger.error("关闭时取消工单失败，网络连接出错，线号：" + line);
			System.exit(0);
		}
	}

	/**
	 * 根据窗口大小调整界面控件大小
	 * 
	 * @param primarystage
	 */
	public void scenceChangeListener(Stage primarystage) {
		primarystage.widthProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				senceWidth = newValue.intValue();
				// 改变工单下拉框和板面类型下拉框的宽度和字体大小
				changeCbSize(senceWidth);
				// 调整文本框控件的大小和字体大小
				changeLbSize(senceWidth);
				// 调整重置工单按钮的位置
				resetBt.setLayoutX(senceWidth / 2 - 140);
				// 调整操作员文本框的大小和位置
				changeOperatorSize(senceWidth);
				// 调整表格的每一列的大小和字体大小
				ObservableList<TableColumn<ResultData, ?>> Columns = DataTv.getColumns();
				for (TableColumn<ResultData, ?> tableColumn : Columns) {
					tableColumn.setMinWidth((senceWidth - 47) / 7);
					tableColumn.setMaxWidth((senceWidth - 47) / 7);
					tableColumn.setStyle("-fx-font-size:" + ((((senceWidth - 18) / 7 - 111) / 20) + 18) + "px;"
							+ "	-fx-alignment: center;" + "	-fx-font-family:'Microsoft YaHei';"
							+ "	-fx-font-weight: bolder;");
				}
				// 调整单元格的字体大小
				if (isUpdate) {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							updateText();

						}
					});
				}
			}
		});
	}

	/**
	 * 改变工单下拉框和板面类型下拉框的宽度和字体大小
	 * 
	 * @param senceWidth
	 */
	private void changeCbSize(int senceWidth) {

		workOrderCb.setMinWidth((senceWidth / 2 - 85));
		workOrderCb.setMaxWidth((senceWidth / 2 - 85));
		workOrderCb.setStyle(
				workOrderCb.getStyle() + "-fx-font-size:" + (20 + (workOrderCb.getMinWidth() - 315) / 200) + ";");
		boardTybeCb.setMinWidth(125 + (senceWidth / 2 - 400) / 10);
		boardTybeCb.setMaxWidth(125 + (senceWidth / 2 - 400) / 10);
		boardTybeCb.setStyle(
				boardTybeCb.getStyle() + "-fx-font-size:" + (20 + (boardTybeCb.getMinWidth() - 125) / 20) + ";");
	}

	/**
	 * 改变操作员文本框和线号下拉框的宽度，位置和字体
	 * 
	 * @param senceWidth
	 */
	private void changeOperatorSize(int senceWidth) {
		// 操作员工号文本框
		operatorLb.setMinWidth(110 + (senceWidth / 2 - 400) / 10);
		operatorLb.setLayoutX(senceWidth / 2 - operatorLb.getMinWidth() - 10);
		operatorNameLb.setLayoutX(senceWidth / 2 - operatorLb.getMinWidth() - 100);
		operatorLb.setStyle(
				operatorLb.getStyle() + "-fx-font-size:" + (22 + (operatorLb.getMinWidth() - 110) / 10) + ";");
		// 线号下拉框
		lineCb.setMinWidth(115 + ((senceWidth / 2 - operatorLb.getMinWidth() - 300) / 50));
		lineCb.setMaxWidth(115 + ((senceWidth / 2 - operatorLb.getMinWidth() - 300) / 50));
		lineCb.setStyle(lineCb.getStyle() + "-fx-font-size:" + (20 + (lineCb.getMinWidth() - 115) / 4) + ";");
	}

	/**
	 * 改变文本框的宽度和字体大小
	 * 
	 * @param senceWidth
	 */
	private void changeLbSize(int senceWidth) {
		String textStyle = "-fx-alignment:center;-fx-font-family:'Microsoft YaHei';"
				+ "-fx-background-radius:4;-fx-border-radius:4;-fx-border-color:gray;-fx-font-weight:bold;";
		double LbSize = ((senceWidth - 20) / 2) - 130;

		lineseatLb.setMinWidth(LbSize);
		scanLineseatLb.setMinWidth(LbSize);
		materialNoLb.setMinWidth(LbSize);
		scanMaterialNoLb.setMinWidth(LbSize);
		double textsize = (22 + ((lineseatLb.getMinWidth() - 260) / 48));
		lineseatLb.setStyle(textStyle + ";-fx-font-size:" + textsize + "px;");
		scanLineseatLb.setStyle(textStyle + ";-fx-font-size:" + textsize + "px;");
		materialNoLb.setStyle(textStyle + ";-fx-font-size:" + textsize + "px;");
		scanMaterialNoLb.setStyle(textStyle + ";-fx-font-size:" + textsize + "px;");

		typeLb.setMinWidth(((senceWidth - 20) / 2) - 10);
		resultLb.setMinWidth(((senceWidth - 20) / 2) - 10);
		typeLb.setStyle(typeLb.getStyle() + "-fx-font-size:140px;-fx-font-weight:bold;");
		resultLb.setStyle(resultLb.getStyle() + "-fx-font-size:140px;-fx-font-weight:bold;");

	}

	/**
	 * 根据包创建日志实体
	 * 
	 * @param p
	 * @return
	 */
	private SocketLog createLogByPackage(BasePackage p) {
		SocketLog log = new SocketLog();
		FieldUtil.copy(p, log);
		log.setTime(new Date());
		String data = BytesParser.parseBytesToHexString(PackageParser.serialize(p));
		log.setData(data);
		return log;
	}

	/**
	 * 发送http请求
	 * 
	 * @param action
	 * @param args
	 * @author HCJ
	 * @return
	 */
	public String sendRequest(String action, Map<String, String> args) {
		try {
			String response = httpHelper.requestHttp(action, args);
			result = response;
		} catch (IOException e) {
			httpFail(action, IS_NETWORK, null);
			e.printStackTrace();
		}
		return result;
	}
}
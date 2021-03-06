package cn.ms.neural;

import java.util.Map;

import cn.ms.neural.common.exception.AlarmException;
import cn.ms.neural.common.exception.ProcessorException;
import cn.ms.neural.common.exception.degrade.DegradeException;
import cn.ms.neural.common.exception.echosound.EchoSoundException;
import cn.ms.neural.common.exception.idempotent.IdempotentException;
import cn.ms.neural.common.exception.neure.NeureBreathException;
import cn.ms.neural.common.exception.neure.NeureCallbackException;
import cn.ms.neural.common.exception.neure.NeureFaultTolerantException;
import cn.ms.neural.moduler.Moduler;
import cn.ms.neural.moduler.ModulerType;
import cn.ms.neural.moduler.extension.blackwhite.processor.IBlackWhiteProcessor;
import cn.ms.neural.moduler.extension.degrade.processor.IDegradeProcessor;
import cn.ms.neural.moduler.extension.echosound.processor.IEchoSoundProcessor;
import cn.ms.neural.moduler.extension.echosound.type.EchoSoundType;
import cn.ms.neural.moduler.extension.flowrate.processor.IFlowRateProcessor;
import cn.ms.neural.moduler.extension.gracestop.processor.IGraceStopProcessor;
import cn.ms.neural.moduler.extension.idempotent.processor.IdempotentProcessor;
import cn.ms.neural.moduler.extension.pipescaling.processor.IPipeScalingProcessor;
import cn.ms.neural.moduler.neure.processor.INeureProcessor;
import cn.ms.neural.moduler.senior.alarm.IAlarmType;
import cn.ms.neural.processor.INeuralProcessor;
import cn.ms.neural.support.AbstractNeuralFactory;

/**
 * 集中式微服务神经元 <br>
 * <br>
 * 注意:使用时请单例化使用<br>
 * 1.泛化引用、泛化实现<br>
 * 2.链路追踪、容量规划、实时监控<br>
 * 3.优雅停机→黑白名单→管道缩放→流量控制→资源鉴权→服务降级→幂等保障→灰度路由→回声探测→[熔断拒绝→超时控制→舱壁隔离→服务容错→慢性尝试]<br>
 * <br>
 * 待实现:
 * <br>
 * 链路追踪<br>
 * 容量规划<br>
 * 实时监控<br>
 * 资源鉴权<br>
 * 灰度路由<br>
 * 
 * @author lry
 *
 * @param <REQ> 请求对象
 * @param <RES> 响应对象
 */
public class NeuralIntegrate<REQ, RES> extends AbstractNeuralFactory<REQ, RES>{

	public NeuralIntegrate(Moduler<REQ, RES> moduler) {
		try {
			super.notify(moduler);//通知节点配置信息
			super.init();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * 微服务神经元
	 * 
	 * @param req 请求对象
	 * @param neuralId 神经元请求ID(用于幂等控制)
	 * @param echoSoundType 回声探测类型
	 * @param blackWhiteIdKeyVals 黑白名单KEY-VALUE
	 * @param processor 微服务神经元处理器
	 * @param args 其他参数
	 * @return
	 */
	public RES neural(REQ req,
			final String neuralId, 
			final EchoSoundType echoSoundType, 
			final Map<String, Object> blackWhiteIdKeyVals, 
			final INeuralProcessor<REQ, RES> processor, Object...args) {
		
		//$NON-NLS-优雅停机开始$
		return moduler.getGraceStop().gracestop(req, new IGraceStopProcessor<REQ, RES>() {
			@Override
			public RES processor(REQ req, Object...args) throws ProcessorException {
				
				//$NON-NLS-黑白名单开始$
				return moduler.getBlackWhite().blackwhite(req, blackWhiteIdKeyVals, new IBlackWhiteProcessor<REQ, RES>() {
					@Override
					public RES processor(REQ req, Object...args) throws ProcessorException {
						
						//$NON-NLS-管道缩放开始$
						return moduler.getPipeScaling().pipescaling(req, new IPipeScalingProcessor<REQ, RES>() {
							@Override
							public RES processor(REQ req, Object...args) throws ProcessorException {
								
								//$NON-NLS-流量控制$
								return moduler.getFlowRate().flowrate(req, new IFlowRateProcessor<REQ, RES>() {
									@Override
									public RES processor(REQ req, Object... args) throws ProcessorException {
										
										//$NON-NLS-服务降级开始$
										return moduler.getDegrade().degrade(req, new IDegradeProcessor<REQ, RES>() {
											@Override
											public RES processor(REQ req, Object...args) throws ProcessorException {
												
												//$NON-NLS-幂等开始$
												return moduler.getIdempotent().idempotent(neuralId, req, new IdempotentProcessor<REQ, RES>() {
													@Override
													public RES processor(REQ req, Object...args) throws ProcessorException {
														
														//$NON-NLS-回声探测开始$
														return moduler.getEchoSound().echosound(echoSoundType, req, new IEchoSoundProcessor<REQ, RES>() {
															@Override
															public RES processor(REQ req, Object...args) throws ProcessorException {
																
																//$NON-NLS-容错内核开始(熔断拒绝→超时控制→舱壁隔离→服务容错→慢性尝试)$
																return moduler.getNeure().neure(req, new INeureProcessor<REQ, RES>() {
																	@Override
																	public RES processor(REQ req, Object...args) throws ProcessorException {//内核业务封装
																		return processor.processor(req, args);
																	}
																	/**
																	 * 内核容错
																	 */
																	@Override
																	public RES faulttolerant(REQ req, Object...args) throws NeureFaultTolerantException{
																		return processor.faulttolerant(req, args);
																	}
																	/**
																	 * 内核呼吸
																	 */
																	@Override
																	public long breath(long nowTimes, long nowExpend, long maxRetryNum, Object...args) throws NeureBreathException {
																		return processor.breath(nowTimes, nowExpend, maxRetryNum, args);
																	}
																	/**
																	 * 内核回调
																	 */
																	@Override
																	public void callback(RES res, Object...args) throws NeureCallbackException {
																		processor.callback(res, args);
																	}
																	/**
																	 * 告警
																	 */
																	@Override
																	public void alarm(IAlarmType alarmType, REQ req, RES res,
																			Throwable t, Object... args) throws AlarmException {
																		processor.alarm(ModulerType.Neure, alarmType, req, res, t, args);																		
																	}
																}, args);//$NON-NLS-容错内核结束$
															}
															/**
															 * 回声探测请求
															 */
															@Override
															public REQ $echo(REQ req, Object...args) throws EchoSoundException {
																return processor.$echo(req, args);
															}
															/**
															 * 回声探测响应
															 */
															@Override
															public RES $rebound(REQ req, Object...args) throws EchoSoundException {
																return processor.$rebound(req, args);
															}
															/**
															 * 告警
															 */
															@Override
															public void alarm(IAlarmType alarmType, REQ req, RES res, Throwable t, Object... args) throws AlarmException {
																processor.alarm(ModulerType.EchoSound, alarmType, req, res, t, args);																
															}
														}, args);//$NON-NLS-回声探测结束$
													}
													/**
													 * 幂等请求校验
													 */
													@Override
													public boolean check(String neuralId, Object...args) throws IdempotentException {
														return processor.check(neuralId);
													}
													/**
													 * 获取幂等数据
													 */
													@Override
													public RES get(String neuralId, Object...args) throws IdempotentException {
														return processor.get(neuralId);
													}
													/**
													 * 幂等持久化数据
													 */
													@Override
													public void storage(REQ req, RES res, Object...args) throws IdempotentException {
														processor.storage(req, res, args);
													}
													/**
													 * 告警
													 */
													@Override
													public void alarm(IAlarmType alarmType, REQ req, RES res, Throwable t, Object... args) throws AlarmException {
														processor.alarm(ModulerType.Idempotent, alarmType, req, res, t, args);														
													}
												}, args);//$NON-NLS-幂等结束$
											}
											/**
											 * 降级mock
											 */
											@Override
											public RES mock(REQ req, Object...args) throws DegradeException {
												return processor.mock(req, args);
											}
											/**
											 * 业务降级
											 */
											@Override
											public RES bizDegrade(REQ req, Object...args) throws DegradeException {
												return processor.bizDegrade(req, args);
											}
											/**
											 * 告警
											 */
											@Override
											public void alarm(IAlarmType alarmType,REQ req, RES res, Throwable t, Object... args) throws AlarmException {
												processor.alarm(ModulerType.Degrade, alarmType, req, res, t, args);												
											}
										}, args);//$NON-NLS-服务降级结束$
									}
									/**
									 * 告警
									 */
									@Override
									public void alarm(IAlarmType alarmType, REQ req, RES res, Throwable t, Object... args) throws AlarmException {
										processor.alarm(ModulerType.FlowRate, alarmType, req, res, t, args);
									}
								}, args);//$NON-NLS-流量控制结束$
							}
							/**
							 * 告警
							 */
							@Override
							public void alarm(IAlarmType alarmType, REQ req, RES res, Throwable t, Object... args) throws AlarmException {
								processor.alarm(ModulerType.PipeScaling, alarmType, req, res, t, args);
							}
						}, args);//$NON-NLS-管道缩放结束$
					}
					/**
					 * 告警
					 */
					@Override
					public void alarm(IAlarmType alarmType, REQ req, RES res, Throwable t, Object... args) throws AlarmException {
						processor.alarm(ModulerType.BlackWhite, alarmType, req, res, t, args);						
					}
				}, args);//$NON-NLS-黑白名单结束$
			}
			/**
			 * 告警
			 */
			@Override
			public void alarm(IAlarmType alarmType, REQ req, RES res, Throwable t, Object... args) throws AlarmException {
				processor.alarm(ModulerType.GraceStop, alarmType, req, res, t, args);
			}
		}, args);//$NON-NLS-优雅停机结束$
	}
	
}

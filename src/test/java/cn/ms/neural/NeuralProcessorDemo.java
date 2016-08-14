package cn.ms.neural;

import cn.ms.neural.common.exception.ProcessorException;
import cn.ms.neural.common.exception.degrade.DegradeException;
import cn.ms.neural.common.exception.echosound.EchoSoundException;
import cn.ms.neural.common.exception.idempotent.IdempotentException;
import cn.ms.neural.common.exception.neure.NeureAlarmException;
import cn.ms.neural.common.exception.neure.NeureBreathException;
import cn.ms.neural.common.exception.neure.NeureCallbackException;
import cn.ms.neural.common.exception.neure.NeureFaultTolerantException;
import cn.ms.neural.moduler.neure.type.AlarmType;
import cn.ms.neural.processor.INeuralProcessor;

public class NeuralProcessorDemo implements INeuralProcessor<String, String> {

	@Override
	public String processor(String req, Object... args) throws ProcessorException {
		return "这是响应报文";
	}

	@Override
	public String mock(String req, Object... args) throws DegradeException {
		return "这是MOCK响应报文";
	}

	@Override
	public String bizProcessor(String req, Object... args) throws DegradeException {
		return "这是业务降级响应报文";
	}

	@Override
	public boolean check(String idempotentKEY) throws IdempotentException {
		return false;
	}

	@Override
	public String get(String idempotentKEY) throws IdempotentException {
		return "这是幂等响应报文";
	}

	@Override
	public void storage(String req, String res, Object... args) throws IdempotentException {
		
	}

	@Override
	public String $echo(String req, Object... args) throws EchoSoundException {
		return "这是回声探测请求报文";
	}

	@Override
	public String $rebound(String req, Object... args) throws EchoSoundException {
		return "这是回声探测响应报文";
	}

	@Override
	public String faulttolerant(String req, Object... args) throws NeureFaultTolerantException {
		return "这是容错响应报文";
	}

	@Override
	public long breath(long nowTimes, long nowExpend, long maxRetryNum, Object... args) throws NeureBreathException {
		return 0;
	}

	@Override
	public void callback(String res, Object... args) throws NeureCallbackException {
		
	}

	@Override
	public void alarm(AlarmType alarmType, String req, Throwable t, Object... args) throws NeureAlarmException {
		
	}

}
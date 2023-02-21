package org.apereo.portal.context;

import java.util.Arrays;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

//import javax.servlet.http.HttpSession;

@Aspect
@Component
//@EnableAspectJAutoProxy(proxyTargetClass = true)
public class HttpSessionAspect {    
	private static final Log log = LogFactory.getLog(HttpSessionAspect.class);
	
	public HttpSessionAspect() {
		log.error("initializing HttpSessionAspect");
	}


//    @Pointcut(value = "execution(public * *(..))")
//    public void anyPublicMethod() {}

//	HttpSession session;
//	@Before("execution(public void com.journaldev.spring.model..set*(*))")
//	@Before("execution(public void javax.servlet.http.HttpSession.setAttribute(*))")
//	@Before("execution(* javax.servlet.http.HttpSession.setAttribute(..))")
//	public void loggingAdvice(JoinPoint joinPoint){
	
	
//    @Pointcut(value = "execution(public * java.servlet.http.HttpSession.setAttribute(..))")
//    public void anyPublicMethod() {}

//  @Around("anyPublicMethod()")
//    @Around("anyPublicMethod() && @annotation(openEntityManager)")
//    public Object openEntityManager(ProceedingJoinPoint pjp, OpenEntityManager openEntityManager)
//            throws Throwable {
	@Around("execution(* javax.servlet.http.HttpSession.setAttribute(..))")
	public void loggingAdvice(JoinPoint joinPoint){
//	  @Before("args(httpSession,..)")
//	  public void logHttpSession(HttpSession httpSession) {
	  log.error("##############################################");
//		log.error("Before running loggingAdvice on method="+joinPoint.toString());		
//		log.error("Agruments Passed=" + Arrays.toString(joinPoint.getArgs()));
		log.error("in loggingAdvice");
		log.error("##############################################");
//		return null;
	}
}

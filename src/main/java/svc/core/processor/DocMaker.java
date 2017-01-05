package svc.core.processor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import svc.core.ann.Action;
import svc.core.ann.Check;
import svc.core.ann.Parm;
import svc.core.ann.Referer;
import svc.core.ann.Return;
import svc.core.ann.ReturnCode;

public abstract class DocMaker extends AbstractProcessor {
	abstract protected Class<? extends Annotation> getCallableType();

	abstract protected Class<?> getCallableReturnType();

	private Pattern _authLevelPattern = Pattern.compile("authLevel=(.*?)[,\\)]");
	private Pattern _valuePattern = Pattern.compile("value=(.*?)[,\\)]");

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

		Class<? extends Annotation> callable_ann = getCallableType();
		Set<? extends Element> ele_callables = env.getElementsAnnotatedWith(getCallableType());
		System.out.println("开始扫描 " + callable_ann.getSimpleName());
		// 遍历所有 @Service
		for (Element ele_callable : ele_callables) {
			CallableInfo callable_info = new CallableInfo();
			// 验证类型是否为类
			if (ele_callable.getKind() != ElementKind.CLASS)
				continue;
			String callable_name = ele_callable.toString();

			// 产生 Service 信息
			Annotation ann_callable = ele_callable.getAnnotation(callable_ann);
			callable_info.name = callable_name;

			callable_info.desc = "";
			callable_info.authLevel = 1;
			try {
				Matcher m = _authLevelPattern.matcher(ann_callable.toString());
				if (m.find()) {
					callable_info.authLevel = Integer.parseInt(m.group(1));
				}
				m = _valuePattern.matcher(ann_callable.toString());
				if (m.find()) {
					callable_info.authLevel = Integer.parseInt(m.group(1));
				}
			} catch (Exception e) {
			}

			// 遍历该 Service 的所有元素
			List<? extends Element> ele_actions = ele_callable.getEnclosedElements();
			for (Element ele_action : ele_actions) {
				// 验证是否为 Method
				if (ele_action.getKind() != ElementKind.METHOD)
					// ele_action.getModifiers() == 2;//方法
					continue;

				Set<javax.lang.model.element.Modifier> mfs = ele_action.getModifiers();
				for (javax.lang.model.element.Modifier mf : mfs) {
					// 过滤掉 不为 public 的方法
					if (!"public".equals(mf.toString())) {
						continue;
					}

					// 返回值判断
					if (!((ExecutableElement) ele_action).getReturnType().toString()
							.equals(getCallableReturnType().getName())) {
						continue;
					}

					Action ann_action = ele_action.getAnnotation(Action.class);

					// 产生 Action 信息
					String action_name = ele_action.getSimpleName().toString();
					ActionInfo action_info = new ActionInfo();
					action_info.name = action_name;
					action_info.desc = ann_action == null ? "" : ann_action.value();
					action_info.authLevel = (ann_action != null && ann_action.authLevel() >= 0) ? ann_action.authLevel()
							: callable_info.authLevel;
					callable_info.actions.add(action_info);

					Referer ann_ref = ele_action.getAnnotation(Referer.class);
					action_info.referer = ann_ref == null ? "" : ann_ref.value();

					// 开始处理参数
					for (VariableElement ee_parm : ((ExecutableElement) ele_action).getParameters()) {
						Parm ann_parm = ee_parm.getAnnotation(Parm.class);
						Check ann_check = ee_parm.getAnnotation(Check.class);
						String parm_name = ee_parm.getSimpleName().toString();
						if (parm_name.endsWith("args"))
							continue; // 忽略原始参数
						String type = ee_parm.asType().toString();

						ParmInfo pi = new ParmInfo();
						pi.name = parm_name;
						// ee_parm.asType().getKind()
						pi.desc = ann_parm == null ? "" : ann_parm.value();
						pi.type = type;
						pi.check = ann_check == null ? "" : ann_check.value();
						pi.checkType = ann_check == null || pi.check.equals("") ? "" : ann_check.type().toString();
						action_info.parameters.add(pi);
					}

					// 开始处理返回值
					Return[] ann_rtns = ele_action.getAnnotationsByType(Return.class);
					for (Return ann_rtn : ann_rtns) {
						ReturnInfo ri = new ReturnInfo();
						ri.name = ann_rtn.value();
						ri.desc = ann_rtn.desc();
						ri.type = ann_rtn.type();
						action_info.returns.add(ri);
					}

					// 开始处理返回代码
					ReturnCode[] ann_codes = ele_action.getAnnotationsByType(ReturnCode.class);
					for (ReturnCode ann_code : ann_codes) {
						ReturnCodeInfo ri = new ReturnCodeInfo();
						String[] code_arr = ann_code.value().split("_", 2);
						if (code_arr.length < 2)
							continue;
						ri.code = Integer.parseInt(code_arr[0]);
						ri.desc = code_arr[1];
						action_info.codes.add(ri);
					}
				}
			}

			try {
				StringBuilder sb = new StringBuilder();
				sb.append(callable_info.name).append("\t").append(callable_info.authLevel).append("\t")
						.append(callable_info.desc).append("\n");
				for (ActionInfo ai : callable_info.actions) {
					sb.append("\tAction\t").append(ai.name).append("\t").append(ai.authLevel).append("\t")
							.append(ai.desc).append("\n");
					sb.append("\t\tReferer\t").append(ai.referer).append("\n");
					for (ParmInfo pi : ai.parameters) {
						sb.append("\t\tParm\t").append(pi.name).append("\t").append(pi.type).append("\t")
								.append(pi.checkType).append("\t").append(pi.check).append("\t").append(pi.desc)
								.append("\n");
					}
					for (ReturnCodeInfo ri : ai.codes) {
						sb.append("\t\tReturnCode\t").append(ri.code).append("\t").append(ri.desc).append("\n");
					}
					for (ReturnInfo ri : ai.returns) {
						sb.append("\t\tReturn\t").append(ri.name).append("\t").append(ri.type).append("\t")
								.append(ri.desc).append("\n");
					}
				}

				String package_name;
				String file_name;
				int pos = callable_name.lastIndexOf('.');
				if (pos == -1) {
					package_name = "default";
					file_name = callable_name;
				} else {
					package_name = callable_name.substring(0, pos);
					file_name = callable_name.substring(pos + 1);
				}

				String package_prefix = callable_ann.getSimpleName();
				FileObject out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
						package_prefix + package_name, file_name);
				System.out.println(
						"Find " + callable_ann.getSimpleName() + ": " + callable_name + " made at " + out.getName());
				out.openWriter().append(sb.toString()).close();
			} catch (IOException e1) {
				System.out.println(callable_ann.getSimpleName() + ": " + callable_name + " 无法保存 ");
				e1.printStackTrace();
			}
		}
		return true;
	}

	// @Override
	// public Set<String> getSupportedAnnotationTypes() {
	// Set<String> result = new HashSet<>();
	// result.add(svc.core.callable.ann.hfjy.base.ann.callable.Controller.class.getName());
	// result.add(svc.core.callable.ann.hfjy.base.ann.callable.Service.class.getName());
	// return result;
	// }

	static public class CallableInfo {
		public String name;
		public String desc;
		public int authLevel;
		public List<ActionInfo> actions = new ArrayList<ActionInfo>();
	}

	static public class ActionInfo {
		public String name;
		public String desc;
		public int authLevel;
		public String referer;
		public List<ParmInfo> parameters = new ArrayList<ParmInfo>();
		public List<ReturnInfo> returns = new ArrayList<ReturnInfo>();
		public List<ReturnCodeInfo> codes = new ArrayList<ReturnCodeInfo>();
	}

	static public class ParmInfo {
		public String name;
		public String desc;
		public String type;
		public String check;
		public String checkType;
	}

	static public class ReturnInfo {
		public String name;
		public String desc;
		public String type;
	}

	static public class ReturnCodeInfo {
		public int code;
		public String desc;
	}

}

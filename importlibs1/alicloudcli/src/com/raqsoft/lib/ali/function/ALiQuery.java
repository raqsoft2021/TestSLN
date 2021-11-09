package com.raqsoft.lib.ali.function;

import com.raqsoft.common.MessageManager;
import com.raqsoft.common.RQException;
import com.raqsoft.dm.Context;
import com.raqsoft.expression.Expression;
import com.raqsoft.expression.Function;
import com.raqsoft.expression.IParam;
import com.raqsoft.expression.Node;
import com.raqsoft.resources.EngineMessage;


// ѡ�� @x ��ѯ������ر�ali_client
// ѡ�� @z ��Χ��ѯ�ǵ����

// selectCol��ѡ����ʱ������������ʱ���������У�ʡ�Ա�ʾѡ�������ֶ�

// filter�����˱��ʽ������һ�²�����
// �߼�������  &&��||��!
// ��ϵ������  >��>=��==��<��<=��!=
// ��ʽ�ǣ�<�ֶ���> ��ϵ������  <ֵ���ʽ>
// ���磺col1 > arg1 && col1 < arg2 || col2 != arg3
// �������ֶ���Ҫ�� <�����ֶ� == false>������ʽ������������<!�����ֶ�>

// ali_query@x(ali_client, tableName, keyName, keyValue, selectCol, filter)
// ������ʱkeyName����������keyValue�����ֵ��鵥������������������������
// ������ʱkeyName�����������ɵ����У�keyValue�����ֵ������鵥������������е����������������

// ali_query@xz(ali_client, tableName, keyName, startValue:endValue, selectCol, filter)
// startValue��endValue��ֵ�����null����ʡ�Ա�ʾ����С�������

public class ALiQuery extends Function {
	public Node optimize(Context ctx) {
		if (param != null) {
			param.optimize(ctx);
		}
		
		return this;
	}

	public Object calculate(Context ctx) {
		IParam param = this.param;
		if (param == null || param.getSubSize() < 3) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("ali_query" + mm.getMessage("function.missingParam"));
		}

		int size = param.getSubSize();
		if (size > 6) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("ali_query" + mm.getMessage("function.invalidParam"));
		}
		
		IParam sub0 = param.getSub(0);
		IParam sub1 = param.getSub(1);
		IParam sub2 = param.getSub(2);
		
		if (sub0 == null || sub1 == null || sub2 == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("ali_query" + mm.getMessage("function.invalidParam"));
		}
		
		Object obj = sub0.getLeafExpression().calculate(ctx);
		if (!(obj instanceof ALiClient)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("ali_query" + mm.getMessage("function.paramTypeError"));
		}
		
		ALiClient client = (ALiClient)obj;
		obj = sub1.getLeafExpression().calculate(ctx);
		if (!(obj instanceof String)) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("ali_query" + mm.getMessage("function.paramTypeError"));
		}
		
		String tableName = (String)obj;
		Object keyName = sub2.getLeafExpression().calculate(ctx);
		
		if (size == 3) {
			return client.queryRange(tableName, keyName, null, null, null, null, ctx, option);
		}
		
		Object selectCol = null;
		Expression filter = null;
		if (size > 4) {
			IParam sub4 = param.getSub(4);
			if (sub4 != null) {
				selectCol = sub4.getLeafExpression().calculate(ctx);
			}
			
			if (size > 5) {
				IParam sub5 = param.getSub(5);
				if (sub5 != null) {
					filter = sub5.getLeafExpression();
				}
			}
		}
		
		IParam sub3 = param.getSub(3);
		if (sub3 == null) {
			return client.queryRange(tableName, keyName, null, null, selectCol, filter, ctx, option);
		} else if (sub3.isLeaf()) {
			Object keyValue = sub3.getLeafExpression().calculate(ctx);
			return client.query(tableName, keyName, keyValue, selectCol, filter, ctx, option);
		} else {
			if (sub3.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("ali_query" + mm.getMessage("function.invalidParam"));
			}
			
			Object startValue = null;
			Object endValue = null;
			
			IParam left = sub3.getSub(0);
			if (left != null) {
				startValue = left.getLeafExpression().calculate(ctx);
			}
			
			IParam right = sub3.getSub(1);
			if (right != null) {
				endValue = right.getLeafExpression().calculate(ctx);
			}
			
			return client.queryRange(tableName, keyName, startValue, endValue, selectCol, filter, ctx, option);
		}
	}
}

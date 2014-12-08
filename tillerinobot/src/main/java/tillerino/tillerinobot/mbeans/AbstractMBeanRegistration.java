package tillerino.tillerinobot.mbeans;

import java.util.HashMap;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

public class AbstractMBeanRegistration implements MBeanRegistration {
	public static class WithNotifications extends NotificationBroadcasterSupport implements MBeanRegistration {
		AbstractMBeanRegistration inner;

		public WithNotifications(Package pack, Class<?> cls, String name) {
			super();
			inner = new AbstractMBeanRegistration(pack, cls != null ? cls : getClass(), name);
		}

		public void postRegister(Boolean registrationDone) {
			inner.postRegister(registrationDone);
		}

		public void preDeregister() throws Exception {
			inner.preDeregister();
		}

		public void postDeregister() {
			inner.postDeregister();
		}

		public ObjectName preRegister(MBeanServer server, ObjectName objectName)
				throws Exception {
			return inner.preRegister(server, objectName);
		}
	}
	
	final Package pack;
	final Class<?> cls;
	final String name;

	public AbstractMBeanRegistration(Package pack, Class<?> cls, String name) {
		super();
		this.pack = pack;
		this.cls = cls;
		this.name = name;
	}

	public AbstractMBeanRegistration(Class<?> pack, Class<?> cls, String name) {
		this(pack.getPackage(), cls, name);
	}

	public AbstractMBeanRegistration() {
		this((Package) null, null, null);
	}

	ObjectName objectName;

	@Override
	public void postRegister(Boolean registrationDone) { }

	@Override
	public void preDeregister() throws Exception { }

	@Override
	public void postDeregister() { }

	@Override
	public ObjectName preRegister(MBeanServer server, ObjectName objectName)
			throws Exception {
		if (objectName != null)
			return objectName;
		return getObjectName(this);
	}

	static HashMap<String, Integer> instanceNumbers = new HashMap<>();

	static synchronized int getInstanceNumber(String name) {
		Integer x = instanceNumbers.get(name);
		if (x == null) {
			x = 0;
		}
		instanceNumbers.put(name, ++x);
		return x;
	}
	
	public static ObjectName getObjectName(AbstractMBeanRegistration.WithNotifications t) throws MalformedObjectNameException {
		return getObjectName(t.inner);
	}

	public static ObjectName getObjectName(AbstractMBeanRegistration t)
			throws MalformedObjectNameException {
		if (t.objectName != null) {
			return t.objectName;
		}

		Class<?> cls = t.cls;
		if (cls == null)
			cls = t.getClass();

		String className = cls.getName();
		className = className.substring(className.lastIndexOf('.') + 1);

		Package pack = t.pack;
		if (pack == null)
			pack = cls.getPackage();

		String nameString = pack.getName() + ":type="
				+ className;

		if (t.name != null) {
			nameString += ",name=" + t.name;
		}

		int instanceNumber = getInstanceNumber(nameString);

		if (instanceNumber > 1) {
			nameString += "-" + instanceNumber;
		}

		return t.objectName = new ObjectName(nameString);
	}
}

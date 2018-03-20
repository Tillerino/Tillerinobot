ArrayList liveobjects = new ArrayList();
ArrayList pings = new ArrayList();
Ping previousPing = null;

abstract class Drawable {
	int age = 0;

	boolean purge();

	void draw();

	long receivedTime;

	int hue;

	int v1() {
		return hue;
	}

	int v2() {
		return 192;
	}

	int v3() {
		return 255 - ageToDist(age) / (width / 2) * 192;
	}
}
class Received extends Drawable {
	int lane;
	boolean purge() {
		return ageToDist(age - 1000) > width / 2;
	}
	void draw() {
		strokeWeight(5);
		colorMode(HSB, 255);
		drawTail(lane, age, v1(), v2(), v3(), -1);
		stroke(color(v1(), v2(), v3()));
		point(width / 2 - ageToDist(age), lane);
	}
	Blip toBlip() {
		Blip blip = new Blip();
		blip.hue = hue;
		blip.lane = lane;
		blip.receivedTime = receivedTime;
		return blip;
	}
}
class Sent extends Drawable {
	int lane;
	boolean purge() {
		return ageToDist(age - 1000) > width / 2;
	}
	void draw() {
		strokeWeight(5);
		colorMode(HSB, 255);
		drawTail(lane, age, v1(), v2(), v3(), 1);
		stroke(color(v1(), v2(), v3()));
		point(width / 2 + ageToDist(age), lane);
	}
	Blip toBlip() {
		Blip blip = new Blip();
		blip.hue = hue;
		blip.lane = lane;
		blip.receivedTime = receivedTime;
		return blip;
	}
}
class Blip extends Drawable {
	int lane;
	int maxAge = 2000;
	boolean purge() {
		return age > maxAge;
	}
	void draw() {
		strokeWeight(3);
		colorMode(HSB, 255);
		stroke(color(v1(), v2(), v3(), (1 - age / maxAge) * 128));
		fill(color(v1(), v2(), v3(), Math.pow(1 - age / 2000, 2) * 64))
		ellipse(width / 2, lane, ageToDist(age) * 4, ageToDist(age) * 4);
	}
}
class Ping extends Drawable {
	int lane;

	int receivedTime;

	int ping;

	Ping earlierPing;

	Ping newerPing;

	boolean purge() {
		return newerPing != null && ageToDist(newerPing.age) > width / 2;
	}

	void draw() {
		colorMode(RGB, 255);
		if (newerPing != null) {
			strokeWeight(4);
			stroke(160, newerPing.opacity(0));
			if (newerPing.age > 1000) {
				// to draw this line exactly as fast as the first line, we need to adjust by the quotient of the distances
				var speed = Math.abs(newerPing.y() - newerPing.lane) / dist(ageToDist(newerPing.age), newerPing.y(), ageToDist(age), y());
				// but never decrease the speed
				speed = Math.max(speed, 1);
				growLine(ageToDist(newerPing.age) + width / 2, newerPing.y(), ageToDist(age) + width / 2, y(), (newerPing.age - 1000) / 1000 * speed);
			}
		}
		stroke(96, opacity(0));
		strokeWeight(2);
		growLine(ageToDist(age) + width / 2, lane, ageToDist(age) + width / 2, y(), age / 1000);
		strokeWeight(8);
		stroke(192, opacity(1000));
		point(ageToDist(age) + width / 2, y());
		fill(192, opacity(1000));
		textAlign(LEFT, CENTER);
		textSize(10);
		text(ping + "ms", ageToDist(age) + width / 2 + 5, earlierPing == null || earlierPing.ping < ping ? y() - 10 : y() + 10);
	}

	Number y() {
		return pingToY(ping);
	}

	Number opacity(int delay) {
		return 128 * Math.pow(Math.min(Math.max(age - delay, 0), 2000) / 2000, 2);
	}

	Number pingToY(int ping) {
		var logScale = Math.max(0, Math.log10(ping) - 1); // 10ms = 0, 1s = 3
		return height - 10 - logScale * height / 9; // 1s = 1/3rd of window height
	}
}
void setup()
{
	size(window.innerWidth, window.innerHeight);
	background(0);
	smooth();
	frameRate(60);
}

void draw()
{
	while (queue.length > 0) {
		var elem = queue.pop();
		Drawable drawable;
		if (elem.received) {
			drawable = new Received();
			drawable.lane = (elem.received.user % height + height) % height;
			drawable.hue = (elem.received.user / 255 % 255 + 255) % 255;
		}
		if (elem.sent) {
			drawable = new Sent();
			drawable.lane = (elem.sent.user % height + height) % height;
			drawable.hue = (elem.sent.user / 255 % 255 + 255) % 255;
			if (elem.sent.ping) {
				var ping = new Ping();
				ping.lane = drawable.lane;
				ping.receivedTime = elem.receivedTime;
				ping.ping = elem.sent.ping;
				if (previousPing != null) {
					previousPing.newerPing = ping;
					ping.earlierPing = previousPing;
				}
				previousPing = ping;
				console.log(ping);
				liveobjects.add(ping);
			}
		}
		if (drawable == null) {
			continue;
		}
		drawable.receivedTime = elem.receivedTime;
		Blip blip = drawable.toBlip();
		console.log(blip);
		if (blip) {
			liveobjects.add(blip);
		}
		liveobjects.add(drawable);
		console.log(drawable);
	}
	colorMode(RGB);
	fill(0);
	noStroke();
	rect(0, 0, width, height);
	var now = Date.now();
	for (int i = 0; i < liveobjects.size(); i++) {
		Drawable obj = liveobjects.get(i);
		long clockAge = now - obj.receivedTime;
		obj.age = clockAge;
	}
	for (int i = 0; i < liveobjects.size(); i++) {
		Drawable obj = liveobjects.get(i);
		if (obj.purge()) {
			liveobjects.remove(i);
			i--;
		} else {
			obj.draw();
		}
	}
}

Number ageToDist(clockAge)
{
	return Math.log1p(clockAge / 30000) * (width / 2);
}

void drawTail(lane, age, v1, v2, v3, sign)
{
	Number opacity = 30;
	for (var time = age - 32; time >= Math.max(0, age - 1000); time -= 32) {
		opacity --;
		stroke(v1, v2, v3, opacity);
		point(ageToDist(time) * sign + width / 2, lane);
	}
}
void growLine(x1, y1, x2, y2, progress) {
	progress = Math.max(0, Math.min(progress, 1));
	line(x1, y1, x1 + (x2 - x1) * progress, y1 + (y2 - y1) * progress);
}
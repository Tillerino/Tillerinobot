ArrayList liveobjects = new ArrayList();
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
		colorMode(RGB, 255);
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
		colorMode(RGB, 255);
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
		colorMode(RGB, 255);
	}
}
void setup()
{
	size(window.innerWidth * 0.9, window.innerHeight * 0.8);
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
	fill(0);
	noStroke();
	rect(0, 0, width, height);
	var now = Date.now();
	for (int i = 0; i < liveobjects.size(); i++) {
		Drawable obj = liveobjects.get(i);
		long clockAge = now - obj.receivedTime;
		if (obj.purge()) {
			liveobjects.remove(i);
			i--;
		} else {
			obj.draw();
			obj.age = clockAge;
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
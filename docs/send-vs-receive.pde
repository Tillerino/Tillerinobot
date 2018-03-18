ArrayList liveobjects = new ArrayList();
abstract class Drawable {
	int age = 0;

	boolean purge();

	void draw();
}
class Received extends Drawable {
	int lane;
	int hue;
	boolean purge() {
		return age > width / 2;
	}
	void draw() {
		colorMode(HSB, 255)
		c = color(hue, 192, 255)
		fill(c);
		rect(width / 2 - age, lane, 5, 5);
		colorMode(RGB, 255);
	}
}
class Sent extends Drawable {
	int lane;
	int hue;
	boolean purge() {
		return age > width / 2;
	}
	void draw() {
		colorMode(HSB, 255)
		c = color(hue, 192, 255)
		fill(c);
		rect(width / 2 + age, lane, 5, 5);
		colorMode(RGB, 255);
	}
}
void setup()
{
	size(640, 240);
	background(0);
	fill(255);
	PFont
	fontA = loadFont("courier");
	textFont(fontA, 14);
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
			drawable.hue = (elem.received.user / 255 % height + height) % height;
		}
		if (elem.sent) {
			drawable = new Sent();
			drawable.lane = (elem.sent.user % height + height) % height;
			drawable.hue = (elem.sent.user / 255 % height + height) % height;
		}
		liveobjects.add(drawable);
		console.log(drawable);
	}
	fill(0, 10);
	rect(0, 0, width, height);
	strokeWeight(0);
	for (int i = 0; i < liveobjects.size(); i++) {
		Drawable obj = liveobjects.get(i);
		if (obj.purge()) {
			liveobjects.remove(i);
			i--;
		} else {
			obj.draw();
			obj.age++;
		}
	}
//	rect(random(0, width), random(0, height), 5, 5);
}
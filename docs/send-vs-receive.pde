ArrayList liveobjects = new ArrayList();
ArrayList texts = new ArrayList();
ArrayList pings = new ArrayList();
Ping previousPing = null;
// the age at which you hit the screen edge
float maxAge = 300000;
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
	// class marker
	boolean received = true;

	long eventId;
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
	// class marker
	boolean sent = true;

	long eventId;
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
class MessageDetails extends Drawable {
	// class marker
	boolean messageDetails = true;

	Received reference;
	int y;
	String message;
	float textWidth;
	boolean purge() {
		return ageToDist(age) - textWidth - 3 > width / 2;
	}
	void draw() {
		if (!showMessageText) {
			return;
		}
		colorMode(HSB, 255);
		var opacity = (age < 1000 ? age / 1000 : Math.max(0.25, (1 - ageToDist(age - 1000) / (width / 2)))) * 192;
		fill(0, opacity / 2);
		noStroke();
		rect(width / 2 - ageToDist(reference.age) + 3, y - 5, textWidth, 10)
		strokeWeight(2);
		stroke(reference.v1(), reference.v2(), reference.v3(), opacity / 2);
		growLine(width / 2 -  ageToDist(reference.age), reference.lane, width / 2 - ageToDist(reference.age), y, age / 1000);
		fill(reference.v1(), reference.v2(), reference.v3(), opacity);
		text(message, width / 2 - ageToDist(reference.age) + 3, y);
	}
	Blip toBlip() {
		return null;
	}
}
class Blip extends Drawable {
	// class marker
	boolean blip = true;

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
	// class marker
	boolean ping = true;

	int lane;

	int receivedTime;

	int ping;

	Ping earlierPing;

	Ping newerPing;

	Sent sentEvent;

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
class Match extends Drawable {
	// class marker
	boolean match = true;

	Received receiveEvent;

	Sent sentEvent;

	int lane;

	int lineLane;

	int drawnFrames = 0;

	boolean purge() {
		return age > 3000;
	}

	void draw() {
		strokeWeight(2);
		colorMode(HSB, 255);
		if (Math.floor(drawnFrames++ / 2) % 2 == 1 && age < 600) {
			return;
		}
		stroke(color(v1(), v2(), v3(), (1 - Math.max(0, age - 1000) / 2000) * 192));
		noFill();
		beginShape();
		vertex(width / 2 - ageToDist(receiveEvent.age), lane);
		vertex(width / 2 - ageToDist(receiveEvent.age), lineLane);
		vertex(width / 2 + ageToDist(sentEvent.age), lineLane);
		vertex(width / 2 + ageToDist(sentEvent.age), lane);
		endShape();
	}
}
void setup()
{
	size(window.innerWidth, window.innerHeight);
	background(0);
	smooth();
	frameRate(60);
	textAlign(LEFT, CENTER);
	textSize(10);
}

void draw()
{
	while (queue.length > 0) {
		var elem = queue.shift();
		Drawable drawable;
		if (elem.received) {
			drawable = new Received();
			drawable.lane = (elem.received.user % height + height) % height;
			drawable.hue = (elem.received.user / 255 % 255 + 255) % 255;
			drawable.eventId = elem.received.eventId;
			/*
			 * look for matching sent object
			 */
			for (var i = 0; i < liveobjects.size(); i++) {
				var obj = liveobjects.get(i);
				if (obj.sent && obj.eventId == drawable.eventId) {
					Match match = new Match();
					match.receiveEvent = drawable;
					match.sentEvent = obj;
					match.receivedTime = elem.receivedTime;
					match.hue = drawable.hue;
					match.lane = drawable.lane;
					avoidCollisionForMatch(match);
					liveobjects.add(match);
				}
			}
		}
		if (elem.sent) {
			drawable = new Sent();
			drawable.lane = (elem.sent.user % height + height) % height;
			drawable.hue = (elem.sent.user / 255 % 255 + 255) % 255;
			drawable.eventId = elem.sent.eventId;
			if (elem.sent.ping) {
				var ping = new Ping();
				ping.lane = drawable.lane;
				ping.receivedTime = elem.receivedTime;
				ping.sentEvent = drawable;
				ping.ping = elem.sent.ping;
				if (previousPing != null) {
					if (previousPing.sentEvent.eventId == drawable.eventId) {
						// more than one sent message logged this ping
						continue;
					}
					previousPing.newerPing = ping;
					ping.earlierPing = previousPing;
				}
				previousPing = ping;
				console.log(ping);
				liveobjects.add(ping);
			}
			/*
			 * look for matching received object
			 */
			for (var i = 0; i < liveobjects.size(); i++) {
				var obj = liveobjects.get(i);
				if (obj.received && obj.eventId == drawable.eventId) {
					Match match = new Match();
					match.receiveEvent = obj;
					match.sentEvent = drawable;
					match.receivedTime = elem.receivedTime;
					match.hue = drawable.hue;
					match.lane = drawable.lane;
					avoidCollisionForMatch(match);
					liveobjects.add(match);
				}
			}
		}
		if (elem.messageDetails) {
			drawable = new MessageDetails();
			drawable.message = elem.messageDetails.message;
			drawable.textWidth = textWidth(drawable.message);
			for (var i = 0; i < liveobjects.size(); i++) {
				if (liveobjects.get(i).received && liveobjects.get(i).eventId == elem.messageDetails.eventId) {
					drawable.reference = liveobjects.get(i);
					break;
				}
			}
			if (drawable.reference == null) {
				continue;
			}
			drawable.y = drawable.reference.lane + 8;
			// collision detection
			for (var i = 0; i < texts.size(); i++) {
				MessageDetails other = texts.get(i);
				if (drawable.y >= other.y + 10 || drawable.y + 10 <= other.y || - ageToDist(maxAge + other.age) + other.textWidth <= - ageToDist(maxAge) - 5 /* five for the little line */) {
					continue;
				}
				// move down, start over
				drawable.y = other.y + 10;
				i = -1;
			}
		}
		if (drawable == null) {
			continue;
		}
		drawable.receivedTime = elem.receivedTime;
		Blip blip = drawable.toBlip();
		if (blip) {
			liveobjects.add(blip);
		}
		if (drawable.messageDetails) {
			texts.add(drawable);
		} else {
			liveobjects.add(drawable);
		}
		console.log(drawable);
	}
	// black out the before we draw the texts
	colorMode(RGB);
	fill(0);
	noStroke();
	rect(0, 0, width / 2, height);
	drawQueue(texts, true);
	// after we've drawn the texts we black out the right half
	colorMode(RGB);
	fill(0);
	noStroke();
	rect(width / 2, 0, width, height);
	drawQueue(liveobjects, false);
}

void drawQueue(ArrayList queue, boolean backward) {
	var now = Date.now();
	/*
	 * adjusted all ages in a first pass to make sure that inter-object drawing
	 * is correct
	 */
	for (int i = 0; i < queue.size(); i++) {
		Drawable obj = queue.get(i);
		long clockAge = now - obj.receivedTime;
		obj.age = clockAge;
	}
	if (backward) {
		for (int i = queue.size() - 1; i >= 0; i--) {
			Drawable obj = queue.get(i);
			if (obj.purge()) {
				queue.remove(i);
			} else {
				obj.draw();
			}
		}
	} else {
		for (int i = 0; i < queue.size(); i++) {
			Drawable obj = queue.get(i);
			if (obj.purge()) {
				queue.remove(i);
				i--;
			} else {
				obj.draw();
			}
		}
	}
}

Number ageToDist(clockAge)
{
	return (1 - Math.pow(0.5, clockAge / 25000)) * (width / 2) + clockAge / 300000 * 6
}

void drawTail(lane, age, v1, v2, v3, sign)
{
	Number opacity = 60;
	for (var time = age - 64; time >= Math.max(0, age - 1000); time -= 64) {
		opacity -= 4;
		stroke(v1, v2, v3, opacity);
		point(ageToDist(time) * sign + width / 2, lane);
	}
}
void growLine(x1, y1, x2, y2, progress) {
	progress = Math.max(0, Math.min(progress, 1));
	line(x1, y1, x1 + (x2 - x1) * progress, y1 + (y2 - y1) * progress);
}
void avoidCollisionForMatch(Match match) {
	match.lineLane = match.lane - 6;
	for (var i = 0; i < liveobjects.size(); i++) {
		var obj = liveobjects.get(i);
		if (!obj.match) {
			continue;
		}
		if (obj.receiveEvent == match.receiveEvent) {
			match.lineLane = obj.lineLane;
			return;
		}
		if (Math.abs(match.lineLane - obj.lineLane) < 6) {
			match.lineLane = obj.lineLane - 6;
			i = -1;
		}
	}
}
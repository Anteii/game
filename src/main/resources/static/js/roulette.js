class Point {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }
}
class Pointer {
    constructor(x, y) {
        // To display pointer
        this.x = x;
        this.y = y;
        // Pointer coords on a circle
        this.realX = x;
        this.realY = y;
        // Angle from 0
        this.angle = 0;
        // How much pointer passed on his way to the point (for debugging)
        this.passed = 0;
        // Point to go to
        this.pointTo = null;
        // Time on its way
        this.time = 60 * 10;
        // Current speed
        this.speed = 0;
        // Current moment of time
        this.moment = 0;
        // Define breaking force
        this.alpha = 0.987;
        // Define length of pointer
        this.k = 1.8;
    }
    setPointerToPoint(point) {
        this.realX = (point.x - roulette.radius) + roulette.radius;
        this.realY = (point.y - roulette.radius) + roulette.radius;
        this.x = (point.x - roulette.radius) / this.k + roulette.radius;
        this.y = (point.y - roulette.radius) / this.k + roulette.radius;
    }
    makeMove() {
        this.angle = (this.angle + this.speed) % (2 * Math.PI);
        this.passed += this.speed;
        this.speed *= this.alpha;
        this.moment++;
        if (this.moment == this.time) {
            console.log(
                "We are in: ",
                new Point(
                    roulette.radius + roulette.radius * Math.cos(this.angle),
                    roulette.radius + roulette.radius * Math.sin(this.angle)
                )
            );
            console.log("We should be in: ", this.pointTo);
            console.log("Current pointer angle is: ", this.angle);
            console.log("Time passed: ", this.moment);
            console.log("Distance passed: ", this.passed);
            this.speed = 0;
            this.passed = 0;
            this.moment = 0;
            this.setPointerToPoint(this.pointTo);
            return;
        }
        this.setPointerToPoint(
            new Point(
                roulette.radius + roulette.radius * Math.cos(this.angle),
                roulette.radius + roulette.radius * Math.sin(this.angle)
            )
        );
    }
}
class Roulette {
    constructor() {
        // Vertices array
        this.coords = [];
        // Radius of roulette
        this.radius = 0;
        for (let i = 0; i < 14; i++) {
            this.coords.push(new Point(0, 0));
        }
    }
    resize(radius) {
        this.radius = radius;
        for (let i = 0; i < 14; i++) {
            this.coords[i].x = radius + radius * Math.cos(2 * Math.PI * i / 14);
            this.coords[i].y = radius + radius * Math.sin(2 * Math.PI * i / 14);
        }
    };
    generatePoint(n) {
        let min = 2 * Math.PI / 14 * (n + 10);
        let max = 2 * Math.PI / 14 * (n + 11);
        let angle = (Math.random() * (max - min) + min) % (2 * Math.PI);
        return new Point(
            this.radius + this.radius * Math.cos(angle),
            this.radius + this.radius * Math.sin(angle)
        );
    }

}

// Setup code
// Proportions to roulette size seems good
const propX = 0.27;
const propY = 0.6;

const roulette = new Roulette();
const pointer = new Pointer();
// Is setup function finished
let isInited = false;
// Center image
let img = null;
const FRAME_RATE = 60;

function preload() {
    img = loadImage('/images/game/volk.jpg');
}

function setup() {

    let rouletteDiameter = Math.max(windowWidth * propX, windowHeight * propY);

    let background = createCanvas(rouletteDiameter, rouletteDiameter);
    background.parent('roulette');

    frameRate(FRAME_RATE);

    roulette.resize(rouletteDiameter / 2);

    pointer.setPointerToPoint(
        new Point((roulette.coords[10].x + roulette.coords[11].x) / 2, (roulette.coords[10].y + roulette.coords[11].y) / 2)
    );
    pointer.angle = getAngleByPoint(roulette.coords[0], new Point(pointer.realX, pointer.realY));

    drawRoulette();
    drawMarks();
    drawPointer();

    isInited = true;
}

function draw() {
    if (pointer.speed != 0) {
        pointer.makeMove();

        background(79, 120, 197);
        drawRoulette();
        drawMarks();
        drawPointer();
    }
}

function windowResized() {
    if (!isInited) return;
    let rouletteDiameter = Math.max(windowWidth * propX, windowHeight * propY);
    resizeCanvas(rouletteDiameter, rouletteDiameter);
    roulette.resize(rouletteDiameter / 2);

    drawRoulette();
    drawMarks();
    drawPointer();
}

function drawRoulette() {
    let xi, yi, xj, yj;
    beginShape();
    strokeWeight(1);
    stroke(0);
    for (let i = 0; i < 14; i++) {
        xi = roulette.coords[i].x;
        yi = roulette.coords[i].y;
        xj = roulette.coords[(i + 1) % 14].x;
        yj = roulette.coords[(i + 1) % 14].y;

        vertex(xi, yi);
        vertex(xj, yj);
        vertex(roulette.radius, roulette.radius);
    }
    endShape(CLOSE);
}

function drawMarks() {
    for (let i = 0; i < 14; i++) {
        x = (roulette.coords[i].x + roulette.coords[(i + 1) % 14].x + roulette.radius) / 3;
        y = (roulette.coords[i].y + roulette.coords[(i + 1) % 14].y + roulette.radius) / 3;
        textAlign(CENTER, CENTER);
        text((i + 4) % 14, x, y);
    }
}

function drawPointer() {
    x1 = pointer;
    strokeWeight(15);
    line(
        pointer.x, pointer.y, roulette.radius, roulette.radius
    );
    translate(roulette.radius, roulette.radius);
    rotate(pointer.angle + Math.PI * 1.5);
    imageMode(CENTER);
    image(img, 0, 0, 100, 100);
}


function movePointerToRandomPoint() {
    let n = Math.floor(Math.random() * 14);
    console.log("Destination sector: ", n);
    movePointerToPoint(roulette.generatePoint(n));
}

function movePointerToPoint(point) {
    let pathLength = calcDistance(point);
    pointer.pointTo = point;
    console.log("distance to destination point", pathLength);
    pointer.speed = pathLength * (1 - pointer.alpha) / (1 - Math.pow(pointer.alpha, pointer.time));
}

function calcDistance(point) {
    return getAngleByPoint(new Point(pointer.realX, pointer.realY), point) +
        Math.floor(Math.random() * 5 + 5) * 2 * Math.PI;
}

function mouseClicked() {
    movePointerToRandomPoint(pointer, roulette);
}

function getAngleByPoint(p1, p2) {
    let l = Math.pow(module(diffPoint(p1, roulette.coords[0])), 2);
    let phi1 = Math.acos(1 - l / (2 * Math.pow(roulette.radius, 2)));
    if (p1.y < roulette.coords[0].y) {
        phi1 = 2 * Math.PI - phi1;
    }

    l = Math.pow(module(diffPoint(p2, roulette.coords[0])), 2);
    let phi2 = Math.acos(1 - l / (2 * Math.pow(roulette.radius, 2)));
    if (p2.y < roulette.coords[0].y) {
        phi2 = 2 * Math.PI - phi2;
    }

    return phi2 >= phi1 ? (phi2 - phi1) : (2 * Math.PI - phi1 + phi2);
}

function module(vec) {
    return Math.sqrt(Math.pow(vec.x, 2) + Math.pow(vec.y, 2));
}

function diffPoint(p1, p2){
    return new Point(p1.x - p2.x, p1.y - p2.y);
}
package scalascenegraph.builders

import java.io._
import java.nio._
import java.awt.image._
import java.awt.{Font => JFont}
import scala.collection.mutable._
import com.jogamp.common.nio._

import scalascenegraph.core._
import scalascenegraph.builders._
import scalascenegraph.core.shapes._
import scalascenegraph.core.Predefs._

trait WorldBuilder {

	// TODO: provide a bind(parent, child) method
	
	private val stack = new Stack[Node]
	private var light: OnOffState = Off
	
	def world(body: => Unit): World = {
		stack.push(new World)
		body
		stack.pop.asInstanceOf[World]
	}
	
	def group(body: => Unit): Group = {
		val group = new Group
		stack.top.attach(group)
		stack.push(group)
		body
		stack.pop.asInstanceOf[Group]
	}

	def color(c: Color) {
		stack.top.addState(new ColorState(c))
	}
	
	def translation(x: Float, y: Float, z: Float) {
		stack.top.addState(new Translation(x, y, z))
	}

	def translation(x: Float, y: Float, z: Float, hook: StateHook[Translation]) {
		stack.top.addState(new DynamicState(hook, new Translation(x, y, z)))
	}
	
	def translation(hook: StateHook[Translation]) {
		stack.top.addState(new DynamicState(hook, new Translation(0.0f, 0.0f, 0.0f)))
	}
	
	def rotation(angle: Float, x: Float, y: Float, z: Float) {
		stack.top.addState(new Rotation(angle, x, y, z))
	}

	def rotation(angle: Float, x: Float, y: Float, z: Float, hook: StateHook[Rotation]) {
		stack.top.addState(new DynamicState(hook, new Rotation(angle, x, y, z)))
	}

	def rotation(hook: StateHook[Rotation]) {
		stack.top.addState(new DynamicState(hook, new Rotation))
	}
	
	def polygonMode(face: Face, mode: DrawingMode) {
		stack.top.addState(new PolygonState(face, mode))
	}
	
	def frontFace(frontFace: FrontFace) {
		stack.top.addState(new FrontFaceState(frontFace))
	}
	
	def cullFace(cullFace: OnOffState) {
		stack.top.addState(new CullFaceState(cullFace))
	}

	def depthTest(depthTest: OnOffState) {
		stack.top.addState(new DepthTestState(depthTest))
	}
	
	def triangle(v1: Vertice, v2: Vertice, v3: Vertice)	{
		stack.top.attach(Triangle(v1, v2, v3))
	}
	
	def triangle(v1: Vertice, v2: Vertice, v3: Vertice,
    		     c1: Color, c2: Color, c3: Color)
	{
		stack.top.attach(Triangle(v1, v2, v3, c1, c2, c3))
	}
	
	def quad(v1: Vertice, v2: Vertice, v3: Vertice, v4: Vertice, color: Color) {
		val c = color
		stack.top.attach(Quad(v1, v2, v3, v4, c, c, c, c))
	}
	
	def quad(v1: Vertice, v2: Vertice, v3: Vertice, v4: Vertice) {
		stack.top.attach(Quad(v1, v2, v3, v4))
	}
	
	def cube {
		stack.top.attach(Cube.create)
	}
	
	// TODO: add type TextureName or something alike
	def cube(textureName: String) {
		val texture = stack.top.getTexture(textureName)
		stack.top.attach(Cube(texture, light))
	}
	
	def cube(color: Color) {
		stack.top.attach(Cube(color))
	}
	
	def cube(colors: Array[Float]) {
		stack.top.attach(Cube(Colors(Buffers.newDirectFloatBuffer(colors))))
	}
	
	def sphere(n: Int, r: Float) {
		stack.top.attach(Sphere(n, r))
	}
	
	def sphere(n: Int, r: Float, color: Color) {
		stack.top.attach(Sphere(n, r, color))
	}
	
	def sphere(n: Int, r: Float, textureName: String) {
		val texture = stack.top.getTexture(textureName)
		stack.top.attach(Sphere(n, r, texture, light))
	}
	
	def box(width: Float, height: Float, depth: Float, l: Int, m: Int, n: Int) {
		val builder = new BoxBuilder(width, height, depth, l, m, n)
		stack.top.attach(builder.createBox)
	}
	
	def light(mode: OnOffState) {
		// FIXME: set the light mode to previous value when exiting the group  
		light = mode
		stack.top.addState(new GlobalLightState(mode))
	}

	def light(instance: LightInstance, mode: OnOffState) {
		stack.top.addState(new LightState(instance, mode))
	}
	
	def light(instance: LightInstance, lightType: LightType, color: Color) {
		stack.top.addState(new LightColorState(instance, lightType, color))
	}

	def light(instance: LightInstance, position: Position) {
		stack.top.addState(new LightPositionState(instance, position))
	}
	
	def shininess(face: Face, shininess: Int) {
		stack.top.addState(new MaterialShininessState(face, shininess))
	}
	
	def ambient(intensity: Intensity) {
		stack.top.addState(new AmbientLightState(intensity))
	}
	
	def material(face: Face, lightType: LightType, color: Color) {
		stack.top.addState(new MaterialState(face, lightType, color))
	}
	
	def lineWidth(width: Float) {
		stack.top.addState(new LineWidthState(width))
	}
	
	def torus(n: Int, R: Float, r: Float) {
		stack.top.attach(Torus(n, R, r))
	}

	def shadeModel(shadeModel: ShadeModel) {
		stack.top.addState(new ShadeModelState(shadeModel))
	}

	def checkerBoard(n: Int, m: Int, c1: Color, c2: Color) {
		stack.top.attach(CheckerBoard(n, m, c1, c2))
	}
	
	def fog(color: Color, mode: FogMode) {
		stack.top.addState(new FogState(color, mode))
	}
	
	def texture(name: String, in: InputStream) {
		val texture = new Texture(in)
		stack.top.addTexture(name, texture)
		stack.top.attach(texture)
	}
	
	def overlay(x: Int, y: Int, image: BufferedImage) {
		// TODO: refactor with method below
		val converted = scalascenegraph.core.Utils.convertImage(image)
		val data = converted.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData
		val buffer = ByteBuffer.allocateDirect(data.length)
		buffer.put(data, 0, data.length)
		buffer.rewind
		stack.top.attach(new ImageOverlay(x, y, image.getWidth, image.getHeight, RGBA, buffer))
	}

	def overlay(x: Int, y: Int, image: BufferedImage, hook: NodeHook[ImageOverlay]) {
		// TODO: refactor with method above
		val converted = scalascenegraph.core.Utils.convertImage(image)
		val data = converted.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData
		val buffer = ByteBuffer.allocateDirect(data.length)
		buffer.put(data, 0, data.length)
		buffer.rewind
		stack.top.attach(new DynamicNode(hook, new ImageOverlay(x, y, image.getWidth, image.getHeight, RGBA, buffer)))
	}

	def overlay(x: Int, y: Int, fontName: String, text: String) {
		val font = stack.top.getFont(fontName)
		stack.top.attach(new TextOverlay(x, y, font, text))
	}

	def overlay(x: Int, y: Int, fontName: String, text: String, hook: NodeHook[TextOverlay]) {
		val font = stack.top.getFont(fontName)
		stack.top.attach(new DynamicNode(hook, new TextOverlay(x, y, font, text)))
	}
	
	def blending(mode: OnOffState) {
		stack.top.addState(new BlendingState(mode))
	}
	
	def font(name: String, jfont: JFont) {
		val font = FontBuilder.create(jfont)
		stack.top.addFont(name, font)
	}
	
	def shader(name: String, shaderType: ShaderType, source: String) {
		val shader = new Shader(shaderType, source)
		stack.top.addShader(name, shader)
		stack.top.attach(shader)
	}
	
	def program(name: String, shaderNames: String*) {
		var shaders = List[Shader]()
		shaderNames.foreach { shaderName => shaders = stack.top.getShader(shaderName) :: shaders }
		val program = new Program(shaders)
		stack.top.addProgram(name, program)
		stack.top.attach(program)
	}
	
	def useProgram(name: String) {
		val program = stack.top.getProgram(name)
		stack.top.addState(new ProgramState(program))
	}
	
}

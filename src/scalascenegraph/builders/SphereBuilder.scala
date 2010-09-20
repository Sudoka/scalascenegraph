package scalascenegraph.builders

import scala.math._
import scala.collection.mutable._
import com.jogamp.common.nio._

import scalascenegraph.core._
import scalascenegraph.core.Predefs._

class SphereBuilder(n: Int, r: Float) {

	def createSphere: Node = {
		val vertices = createVertices
		val normals = createNormals
		new Node {
			def render(context: Context) {
				context.renderer.quads(vertices, normals)
			}
		}
	}

	def createSphere(color: Color): Node = {
		val vertices = createVertices
		new Node {
			def render(context: Context) {
				context.renderer.quads(vertices, color)
			}
		}
	}

	def createSphere(colors: Colors): Node = {
		val vertices = createVertices
		new Node {
			def render(context: Context) {
				context.renderer.quads(vertices, colors)
			}
		}
	}

	def createSphere(texture: Texture): Node = {
		createSphere(texture, Off)
	}

	def createSphere(texture: Texture, light: OnOffState): Node = {
		val vertices = createVertices
		val textureCoordinates = createTextureCoordinates
		light match {
			case Off => new Node {
				def render(context: Context) {
					context.renderer.quads(vertices, textureCoordinates, texture)
				}
			}
			case On => {
				val normals = createNormals
				new Node {
					def render(context: Context) {
						context.renderer.quads(vertices, textureCoordinates, texture, normals)
					}
				}
			}
		}
	}
	
	implicit def doubleToFloat(d: Double): Float = d.asInstanceOf[Float]
	
	private def sphere(teta: Float, phi: Float): Vertice = {
		// cf http://en.wikipedia.org/wiki/Sphere
		val x = r * sin(teta) * cos(phi)
		val y = r * sin(teta) * sin(phi)
		val z = r * cos(teta)
		Vertice(x, y, z)
	}
	
	def createVertices = {
		val ab = new ArrayBuffer[Float]
		val stepAngle = Pi / n
		for (tetaStep <- 0 to n) {
			for (phiStep <- 0 to 2*n) {
				val teta = tetaStep * stepAngle
				val phi = phiStep * stepAngle
				ab ++= sphere(teta, phi).asFloatArray
				ab ++= sphere(teta+stepAngle, phi).asFloatArray
				ab ++= sphere(teta+stepAngle, phi+stepAngle).asFloatArray
				ab ++= sphere(teta, phi+stepAngle).asFloatArray
			}
		}
		Vertices(Buffers.newDirectFloatBuffer(ab.toArray))
	}
	
	def createNormals = Normals(createVertices.floatBuffer)
	
	def createTextureCoordinates = {
		val ab = new ArrayBuffer[Float]
		for (i <- 0 to n) {
			for (j <- 0 to 2*n) {
				ab ++= Array(1.0f * i / n, 1.0f * j / n)
				ab ++= Array(1.0f * (i+1) / n, 1.0f * j / n)
				ab ++= Array(1.0f * (i+1) / n, 1.0f * (j+1) / n)
				ab ++= Array(1.0f * i / n, 1.0f * (j+1) / n)
			}
		}
		TextureCoordinates(Buffers.newDirectFloatBuffer(ab.toArray))
	}
	
}


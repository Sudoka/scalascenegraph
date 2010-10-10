package scalascenegraph.examples

import java.nio._
import java.awt.{Color => JColor}
import java.awt.{Font => JFont}
import java.awt.image._
import com.jogamp.common.nio._
import scala.math._
import scala.collection.mutable._
import javax.media.opengl.GL._
import javax.media.opengl.GL2._
import javax.media.opengl.GL2GL3._
import javax.media.opengl.GL2ES1._
import javax.media.opengl.GL2ES2._
import javax.media.opengl.fixedfunc._
import javax.media.opengl.fixedfunc.GLLightingFunc._
import javax.media.opengl.fixedfunc.GLPointerFunc._
import javax.media.opengl.fixedfunc.GLMatrixFunc._

import scalascenegraph.core._
import scalascenegraph.core.Utils._
import scalascenegraph.core.Predefs._
import scalascenegraph.builders._

class Example11 extends Example with WorldBuilder {
	
	val sand = getClass.getResourceAsStream("/scalascenegraph/examples/sand.jpg")
	val waveVertexShader = getStreamAsString(getClass.getResourceAsStream("/scalascenegraph/examples/Example11.vert"))
	
	val uniformHook = (u: UniformState, c: Context) => {
		// time elapsed in seconds
		u.value = c.elapsed / 1000.0f
	}
	
	
	def example =
		world {
			texture("sand", sand)
			blending(On)
			cullFace(On)
    		depthTest(On)
    		group {
				translation(0.0f, 0.0f, -5.0f)
				quad(
					Vertice3D(2.0f, 2.0f, 0.0f),
					Vertice3D(-2.0f, 2.0f, 0.0f),
					Vertice3D(-2.0f, -2.0f, -0.0f),
					Vertice3D(2.0f, -2.0f, 0.0f),
					"sand")
			}
    		group {
    			color(Color(1.0f, 1.0f, 1.0f, 0.0f))
				translation(0.0f, 0.0f, -4.0f)
				shader("waveVertexShader", GL_VERTEX_SHADER, waveVertexShader)
				program("waveProgram", "waveVertexShader")
				uniform("waveProgram", "t")
				useProgram("waveProgram")
				setUniform("t", 0.0f, uniformHook)
				grid(4.0f, 4.0f, 100, 100)
			}
		}
	
}

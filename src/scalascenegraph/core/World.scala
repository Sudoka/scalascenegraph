package scalascenegraph.core

class World(val foreground: Color = Color.white,
            val background: Color = Color.grey)
extends Group {

    override def render(renderer: Renderer) {
        renderer.clear
        renderer.clearColor(background);
        renderer.color(foreground)
        super.render(renderer)
        renderer.flush
    }
  
}
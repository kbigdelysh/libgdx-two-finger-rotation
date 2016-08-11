package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	//Texture img;
	private BitmapFont font;
	public ModelBatch modelBatch;
	public PerspectiveCamera camera;
	public Model model;
	public ModelInstance instance;
	private Environment environment;
	private MyCameraInputController cameraController;

	@Override
	public void create () {
		//modelBatch = new ModelBatch();
		modelBatch = new ModelBatch(new DefaultShaderProvider() {
			@Override
			protected Shader createShader(Renderable renderable) {
				return new WireframeShader(renderable, config);
			}
		});

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0f, 0f, 10f);
		camera.lookAt(0,0,0);
		camera.near = 1f;
		camera.far = 300f;

		camera.update();

		ModelBuilder modelBuilder = new ModelBuilder();
//		model = modelBuilder.createBox(5.0f, 5.0f, 5.0f,
//				new Material(),
//				VertexAttributes.Usage.Position| VertexAttributes.Usage.Normal);

		model = modelBuilder.createBox(5.0f, 5.0f, 5.0f,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				VertexAttributes.Usage.Position| VertexAttributes.Usage.Normal);
		instance = new ModelInstance(model);

		// create environment for lighting
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		// Add camera controller
		//cameraController = new CameraInputController(camera);
		cameraController = new MyCameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);
		//Gdx.input.setInputProcessor(new GestureDetector(new MyGestureListener()));

		batch = new SpriteBatch();
		//img = new Texture("badlogic.jpg");
		font = new BitmapFont();
		font.setColor(Color.BLUE);
	}

	@Override
	public void render () {

		cameraController.update();


//		Gdx.gl.glClearColor(0, 0, 0, 0);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(camera);
		modelBatch.render(instance);//,environment);
		modelBatch.end();

		batch.begin();

		//batch.draw(img, 0, 0);
		font.getData().setScale(6.0f);
		font.draw(batch, "Hello World from libgdx running in a fragment! :)", 100, 300);

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		//img.dispose();
		modelBatch.dispose();
		model.dispose();
	}
}

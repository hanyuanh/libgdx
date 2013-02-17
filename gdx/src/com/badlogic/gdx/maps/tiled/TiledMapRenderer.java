package com.badlogic.gdx.maps.tiled;

import static com.badlogic.gdx.graphics.g2d.SpriteBatch.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

public interface TiledMapRenderer extends MapRenderer {
	
	public void renderObject(MapObject object);
	public void renderTileLayer(TiledMapTileLayer layer);
	
	public abstract class BatchTiledMapRenderer implements TiledMapRenderer {
		
		protected TiledMap map;

		protected float unitScale;
		
		protected SpriteBatch spriteBatch;
		
		protected Rectangle viewBounds; 

		public TiledMap getMap() {
			return map;			
		}
		
		public float getUnitScale() {
			return unitScale;
		}
		
		public SpriteBatch getSpriteBatch() {
			return spriteBatch;
		}

		public Rectangle getViewBounds() {
			return viewBounds;
		}
		
		public BatchTiledMapRenderer(TiledMap map) {
			this.map = map;
			this.unitScale = 1;
			this.spriteBatch = new SpriteBatch();
			this.viewBounds = new Rectangle();
		}
		
		public BatchTiledMapRenderer(TiledMap map, float unitScale) {
			this.map = map;
			this.unitScale = unitScale;
			this.viewBounds = new Rectangle();
			this.spriteBatch = new SpriteBatch();
		}
		
		@Override
		public void setView(OrthographicCamera camera) {
			spriteBatch.setProjectionMatrix(camera.combined);
			float width = camera.viewportWidth * camera.zoom;
			float height = camera.viewportHeight * camera.zoom;
			viewBounds.set(camera.position.x - width / 2, camera.position.y - height / 2, width, height);
		}
		
		@Override
		public void setView (Matrix4 projection, float x, float y, float width, float height) {
			spriteBatch.setProjectionMatrix(projection);
			viewBounds.set(x, y, width, height);
		}
		
		@Override
		public void render () {
			spriteBatch.begin();
			for (MapLayer layer : map.getLayers()) {
				if (layer.getVisible()) {
					if (layer instanceof TiledMapTileLayer) {
						renderTileLayer((TiledMapTileLayer) layer);
					} else {
						for (MapObject object : layer.getObjects()) {
							renderObject(object);
						}
					}					
				}				
			}
			spriteBatch.end();
		}
		
		@Override
		public void render (int[] layers) {
			spriteBatch.begin();
			for (int layerIdx : layers) {
				MapLayer layer = map.getLayers().getLayer(layerIdx);
				if (layer.getVisible()) {
					if (layer instanceof TiledMapTileLayer) {
						renderTileLayer((TiledMapTileLayer) layer);
					} else {
						for (MapObject object : layer.getObjects()) {
							renderObject(object);
						}
					}					
				}				
			}		
			spriteBatch.end();
		}
	}
	
	public class IsometricTiledMapRenderer extends  BatchTiledMapRenderer {

		private TiledMap map;
		
		private float[] vertices = new float[20];
		
		public IsometricTiledMapRenderer(TiledMap map) {
			super(map);
		}
		
		public IsometricTiledMapRenderer(TiledMap map, float unitScale) {
			super(map, unitScale);
		}	
		
		@Override
		public void renderObject (MapObject object) {
			
		}
		
		@Override
		public void renderTileLayer (TiledMapTileLayer layer) {

			final float color = Color.toFloatBits(1, 1, 1, layer.getOpacity());
			
			int col1 = 0;
			int col2 = layer.getWidth() - 1;
			
			int row1 = 0;
			int row2 = layer.getHeight() - 1;
			
			float tileWidth = layer.getTileWidth() * unitScale;
			float tileHeight = layer.getTileHeight() * unitScale;
			float halfTileWidth = tileWidth * 0.5f;
			float halfTileHeight = tileHeight * 0.5f;
			
			for (int row = row2; row >= row1; row--) {
				for (int col = col1; col <= col2; col++) {
					float x = (col * halfTileWidth) + (row * halfTileWidth);
					float y = (row * halfTileHeight) - (col * halfTileHeight);

					final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
					final TiledMapTile tile = cell.getTile();
					if (tile != null) {
						
						final boolean flipX = cell.getFlipHorizontally();
						final boolean flipY = cell.getFlipVertically();
						final int rotations = cell.getRotation();
						
						TextureRegion region = tile.getTextureRegion();
						
						float x1 = x;
						float y1 = y;
						float x2 = x1 + region.getRegionWidth() * unitScale;
						float y2 = y1 + region.getRegionHeight() * unitScale;
						
						float u1 = region.getU();
						float v1 = region.getV2();
						float u2 = region.getU2();
						float v2 = region.getV();
						
						vertices[X1] = x1;
						vertices[Y1] = y1;
						vertices[C1] = color;
						vertices[U1] = u1;
						vertices[V1] = v1;
						
						vertices[X2] = x1;
						vertices[Y2] = y2;
						vertices[C2] = color;
						vertices[U2] = u1;
						vertices[V2] = v2;
						
						vertices[X3] = x2;
						vertices[Y3] = y2;
						vertices[C3] = color;
						vertices[U3] = u2;
						vertices[V3] = v2;
						
						vertices[X4] = x2;
						vertices[Y4] = y1;
						vertices[C4] = color;
						vertices[U4] = u2;
						vertices[V4] = v1;							
						
						if (flipX) {
							float temp = vertices[U1];
							vertices[U1] = vertices[U3];
							vertices[U3] = temp;
							temp = vertices[U2];
							vertices[U2] = vertices[U4];
							vertices[U4] = temp;
						}
						if (flipY) {
							float temp = vertices[V1];
							vertices[V1] = vertices[V3];
							vertices[V3] = temp;
							temp = vertices[V2];
							vertices[V2] = vertices[V4];
							vertices[V4] = temp;
						}
						if (rotations != 0) {
							switch (rotations) {
								case Cell.ROTATE_90: {
									float tempV = vertices[V1];
									vertices[V1] = vertices[V2];
									vertices[V2] = vertices[V3];
									vertices[V3] = vertices[V4];
									vertices[V4] = tempV;

									float tempU = vertices[U1];
									vertices[U1] = vertices[U2];
									vertices[U2] = vertices[U3];
									vertices[U3] = vertices[U4];
									vertices[U4] = tempU;									
									break;
								}
								case Cell.ROTATE_180: {
									float tempU = vertices[U1];
									vertices[U1] = vertices[U3];
									vertices[U3] = tempU;
									tempU = vertices[U2];
									vertices[U2] = vertices[U4];
									vertices[U4] = tempU;									
									float tempV = vertices[V1];
									vertices[V1] = vertices[V3];
									vertices[V3] = tempV;
									tempV = vertices[V2];
									vertices[V2] = vertices[V4];
									vertices[V4] = tempV;
									break;
								}
								case Cell.ROTATE_270: {
									float tempV = vertices[V1];
									vertices[V1] = vertices[V4];
									vertices[V4] = vertices[V3];
									vertices[V3] = vertices[V2];
									vertices[V2] = tempV;

									float tempU = vertices[U1];
									vertices[U1] = vertices[U4];
									vertices[U4] = vertices[U3];
									vertices[U3] = vertices[U2];
									vertices[U2] = tempU;									
									break;
								}
							}								
						}
						spriteBatch.draw(region.getTexture(), vertices, 0, 20);
					}					
				}
			}
		}
		
	}
	
	public class OrthogonalTiledMapRenderer extends BatchTiledMapRenderer {
		
		private float[] vertices = new float[20];
		
		public OrthogonalTiledMapRenderer(TiledMap map) {
			super(map);
		}

		public OrthogonalTiledMapRenderer(TiledMap map, float unitScale) {
			super(map, unitScale);
		}		
		
		@Override
		public void renderObject (MapObject object) {
			
		}

		@Override
		public void renderTileLayer (TiledMapTileLayer layer) {
			
			final float color = Color.toFloatBits(1, 1, 1, layer.getOpacity());
			
			final int layerWidth = layer.getWidth();
			final int layerHeight = layer.getHeight();
			
			final float layerTileWidth = layer.getTileWidth() * unitScale;
			final float layerTileHeight = layer.getTileHeight() * unitScale;
			
			final int col1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
			final int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));

			final int row1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
			final int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));				
			
			for (int row = row1; row < row2; row++) {
				for (int col = col1; col < col2; col++) {
					final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
					final TiledMapTile tile = cell.getTile();
					if (tile != null) {
						
						final boolean flipX = cell.getFlipHorizontally();
						final boolean flipY = cell.getFlipVertically();
						final int rotations = cell.getRotation();
						
						TextureRegion region = tile.getTextureRegion();
						
						float x1 = col * layerTileWidth;
						float y1 = row * layerTileHeight;
						float x2 = x1 + region.getRegionWidth() * unitScale;
						float y2 = y1 + region.getRegionHeight() * unitScale;
						
						float u1 = region.getU();
						float v1 = region.getV2();
						float u2 = region.getU2();
						float v2 = region.getV();
						
						vertices[X1] = x1;
						vertices[Y1] = y1;
						vertices[C1] = color;
						vertices[U1] = u1;
						vertices[V1] = v1;
						
						vertices[X2] = x1;
						vertices[Y2] = y2;
						vertices[C2] = color;
						vertices[U2] = u1;
						vertices[V2] = v2;
						
						vertices[X3] = x2;
						vertices[Y3] = y2;
						vertices[C3] = color;
						vertices[U3] = u2;
						vertices[V3] = v2;
						
						vertices[X4] = x2;
						vertices[Y4] = y1;
						vertices[C4] = color;
						vertices[U4] = u2;
						vertices[V4] = v1;							
						
						if (flipX) {
							float temp = vertices[U1];
							vertices[U1] = vertices[U3];
							vertices[U3] = temp;
							temp = vertices[U2];
							vertices[U2] = vertices[U4];
							vertices[U4] = temp;
						}
						if (flipY) {
							float temp = vertices[V1];
							vertices[V1] = vertices[V3];
							vertices[V3] = temp;
							temp = vertices[V2];
							vertices[V2] = vertices[V4];
							vertices[V4] = temp;
						}
						if (rotations != 0) {
							switch (rotations) {
								case Cell.ROTATE_90: {
									float tempV = vertices[V1];
									vertices[V1] = vertices[V2];
									vertices[V2] = vertices[V3];
									vertices[V3] = vertices[V4];
									vertices[V4] = tempV;

									float tempU = vertices[U1];
									vertices[U1] = vertices[U2];
									vertices[U2] = vertices[U3];
									vertices[U3] = vertices[U4];
									vertices[U4] = tempU;									
									break;
								}
								case Cell.ROTATE_180: {
									float tempU = vertices[U1];
									vertices[U1] = vertices[U3];
									vertices[U3] = tempU;
									tempU = vertices[U2];
									vertices[U2] = vertices[U4];
									vertices[U4] = tempU;									
									float tempV = vertices[V1];
									vertices[V1] = vertices[V3];
									vertices[V3] = tempV;
									tempV = vertices[V2];
									vertices[V2] = vertices[V4];
									vertices[V4] = tempV;
									break;
								}
								case Cell.ROTATE_270: {
									float tempV = vertices[V1];
									vertices[V1] = vertices[V4];
									vertices[V4] = vertices[V3];
									vertices[V3] = vertices[V2];
									vertices[V2] = tempV;

									float tempU = vertices[U1];
									vertices[U1] = vertices[U4];
									vertices[U4] = vertices[U3];
									vertices[U3] = vertices[U2];
									vertices[U2] = tempU;									
									break;
								}
							}								
						}
						spriteBatch.draw(region.getTexture(), vertices, 0, 20);
					}
				}
			}			
		}
		
	}

	public class OrthogonalTiledMapRenderer2 implements TiledMapRenderer {

		protected TiledMap map;

		protected float unitScale;
		
		protected SpriteCache spriteCache;
		
		protected Rectangle viewBounds; 
		
		private float[] vertices = new float[20];
		
		public boolean recache;
		
		public OrthogonalTiledMapRenderer2(TiledMap map) {
			this.map = map;
			this.unitScale = 1;
			this.spriteCache = new SpriteCache(4000, true);
			this.viewBounds = new Rectangle();
		}
		
		public OrthogonalTiledMapRenderer2(TiledMap map, float unitScale) {
			this.map = map;
			this.unitScale = unitScale;
			this.viewBounds = new Rectangle();
			this.spriteCache = new SpriteCache(4000, true);
		}
		
		@Override
		public void setView(OrthographicCamera camera) {
			spriteCache.setProjectionMatrix(camera.combined);
			float width = camera.viewportWidth * camera.zoom;
			float height = camera.viewportHeight * camera.zoom;
			viewBounds.set(camera.position.x - width / 2, camera.position.y - height / 2, width, height);
			recache = true;
		}
		
		@Override
		public void setView (Matrix4 projection, float x, float y, float width, float height) {
			spriteCache.setProjectionMatrix(projection);
			viewBounds.set(x, y, width, height);
			recache = true;
		}

		public void begin () {
			if (recache) {
				cached = false;
				recache = false;
				spriteCache.clear();
			}
			if (!cached) {
				spriteCache.beginCache();	
			} else {
				Gdx.gl.glEnable(GL10.GL_BLEND);
				Gdx.gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				spriteCache.begin();
			}
			
		}

		public void end () {
			if (!cached) {
				spriteCache.endCache();
				cached = true;
				begin();
				render();
				end();
			} else {
				spriteCache.end();
				Gdx.gl.glDisable(GL10.GL_BLEND);
			}
		}

		@Override
		public void render () {
			begin();
			if (cached) {
				spriteCache.draw(0);
			} else {
				for (MapLayer layer : map.getLayers()) {
					if (layer.getVisible()) {
						if (layer instanceof TiledMapTileLayer) {
							renderTileLayer((TiledMapTileLayer) layer);
						} else {
							for (MapObject object : layer.getObjects()) {
								renderObject(object);
							}
						}					
					}				
				}				
			}
			end();
		}
		
		@Override
		public void render (int[] layers) {
			// FIXME not implemented
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public void renderObject (MapObject object) {
			// TODO Auto-generated method stub
			
		}

		boolean cached = false;
		int count = 0;
		@Override
		public void renderTileLayer (TiledMapTileLayer layer) {
			final float color = Color.toFloatBits(1, 1, 1, layer.getOpacity());
		
			final int layerWidth = layer.getWidth();
			final int layerHeight = layer.getHeight();
			
			final float layerTileWidth = layer.getTileWidth() * unitScale;
			final float layerTileHeight = layer.getTileHeight() * unitScale;
			
			final int col1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
			final int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));

			final int row1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
			final int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));				
			
			for (int row = row1; row < row2; row++) {
				for (int col = col1; col < col2; col++) {
					final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
					final TiledMapTile tile = cell.getTile();
					if (tile != null) {
						count++;
						final boolean flipX = cell.getFlipHorizontally();
						final boolean flipY = cell.getFlipVertically();
						final int rotations = cell.getRotation();
						
						TextureRegion region = tile.getTextureRegion();
						
						float x1 = col * layerTileWidth;
						float y1 = row * layerTileHeight;
						float x2 = x1 + region.getRegionWidth() * unitScale;
						float y2 = y1 + region.getRegionHeight() * unitScale;
						
						float u1 = region.getU();
						float v1 = region.getV2();
						float u2 = region.getU2();
						float v2 = region.getV();
						
						vertices[X1] = x1;
						vertices[Y1] = y1;
						vertices[C1] = color;
						vertices[U1] = u1;
						vertices[V1] = v1;
						
						vertices[X2] = x1;
						vertices[Y2] = y2;
						vertices[C2] = color;
						vertices[U2] = u1;
						vertices[V2] = v2;
						
						vertices[X3] = x2;
						vertices[Y3] = y2;
						vertices[C3] = color;
						vertices[U3] = u2;
						vertices[V3] = v2;
						
						vertices[X4] = x2;
						vertices[Y4] = y1;
						vertices[C4] = color;
						vertices[U4] = u2;
						vertices[V4] = v1;							
						
						if (flipX) {
							float temp = vertices[U1];
							vertices[U1] = vertices[U3];
							vertices[U3] = temp;
							temp = vertices[U2];
							vertices[U2] = vertices[U4];
							vertices[U4] = temp;
						}
						if (flipY) {
							float temp = vertices[V1];
							vertices[V1] = vertices[V3];
							vertices[V3] = temp;
							temp = vertices[V2];
							vertices[V2] = vertices[V4];
							vertices[V4] = temp;
						}
						if (rotations != 0) {
							switch (rotations) {
								case Cell.ROTATE_90: {
									float tempV = vertices[V1];
									vertices[V1] = vertices[V2];
									vertices[V2] = vertices[V3];
									vertices[V3] = vertices[V4];
									vertices[V4] = tempV;

									float tempU = vertices[U1];
									vertices[U1] = vertices[U2];
									vertices[U2] = vertices[U3];
									vertices[U3] = vertices[U4];
									vertices[U4] = tempU;									
									break;
								}
								case Cell.ROTATE_180: {
									float tempU = vertices[U1];
									vertices[U1] = vertices[U3];
									vertices[U3] = tempU;
									tempU = vertices[U2];
									vertices[U2] = vertices[U4];
									vertices[U4] = tempU;									
									float tempV = vertices[V1];
									vertices[V1] = vertices[V3];
									vertices[V3] = tempV;
									tempV = vertices[V2];
									vertices[V2] = vertices[V4];
									vertices[V4] = tempV;
									break;
								}
								case Cell.ROTATE_270: {
									float tempV = vertices[V1];
									vertices[V1] = vertices[V4];
									vertices[V4] = vertices[V3];
									vertices[V3] = vertices[V2];
									vertices[V2] = tempV;

									float tempU = vertices[U1];
									vertices[U1] = vertices[U4];
									vertices[U4] = vertices[U3];
									vertices[U3] = vertices[U2];
									vertices[U2] = tempU;									
									break;
								}
							}								
						}
						spriteCache.add(region.getTexture(), vertices, 0, 20);
					}
				}
			}
		}
		
	}
}

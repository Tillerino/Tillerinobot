package tillerino.tillerinobot.rest;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DelegatingBeatmapsService implements BeatmapsService {
	private final BeatmapsService delegate;

	@Override
	public BeatmapResource byId(int id) {
		return delegate.byId(id);
	}

	@Override
	public BeatmapResource byHash(String hash) {
		return delegate.byHash(hash);
	}
}

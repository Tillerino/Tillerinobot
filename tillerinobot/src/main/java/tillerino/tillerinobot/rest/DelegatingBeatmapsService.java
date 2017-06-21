package tillerino.tillerinobot.rest;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DelegatingBeatmapsService implements BeatmapsService {
	private final BeatmapsService delegate;

	public BeatmapResource byId(int id) {
		return delegate.byId(id);
	}

	public BeatmapResource byHash(String hash) {
		return delegate.byHash(hash);
	}
}

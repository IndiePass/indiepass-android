package com.indieweb.indigenous;

import android.content.Context;

import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.reader.Reader;
import com.indieweb.indigenous.reader.ReaderFactory;
import com.indieweb.indigenous.reader.TimelineActivity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.indieweb.indigenous.users.AuthActivity.INDIEWEB_ACCOUNT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ParseTest {

    @Test
    public void testIndieWebTimelineParsing() {
        User user = new User();
        user.setValid(true);
        user.setAccountType(INDIEWEB_ACCOUNT_TYPE);
        user.setName("testing");
        Reader r = ReaderFactory.getReader(user, null, null);

        String channelsResponse = "{\"channels\":[{\"uid\":\"3\",\"name\":\"Comics\",\"unread\":0},{\"uid\":\"1\",\"name\":\"IndieWeb\",\"unread\":0},{\"uid\":\"4\",\"name\":\"Podcasts\",\"unread\":0},{\"uid\":\"5\",\"name\":\"Science\",\"unread\":0}]}";
        List<Channel> channels = r.parseChannelResponse(channelsResponse, false);
        assertEquals("Comics", channels.get(0).getName());
        assertEquals("IndieWeb", channels.get(1).getName());
        assertEquals("Podcasts", channels.get(2).getName());
        assertEquals("Science", channels.get(3).getName());

        Context context = mock(TimelineActivity.class);
        r = ReaderFactory.getReader(user, null, context);
        List<String> entries = new ArrayList<>();
        String[] olderItems = new String[1];
        String timelineResponse = "{\"paging\":{\"after\":1},\"items\":[{\"type\":\"entry\",\"author\":{\"name\":wumo,\"url\":\"http:\\/\\/wumo.com\\/wumo\",\"photo\":\"null\"},\"uid\":\"http:\\/\\/wumo.com\\/wumo\\/2020\\/08\\/02\",\"url\":\"http:\\/\\/wumo.com\\/wumo\\/2020\\/08\\/02\",\"published\":\"2020-08-01T23:19:08+00:00\",\"content\":{\"html\":\"\\u003Ca href=\\u0022http:\\/\\/wumo.com\\/wumo\\/2020\\/08\\/02\\u0022\\u003E\\n                    \\u003Cimg src=\\u0022http:\\/\\/wumo.com\\/img\\/wumo\\/2020\\/08\\/wumo5efeff95602f18.04568628.jpg\\u0022 alt=\\u0022Wumo 02. Aug 2020\\u0022 \\/\\u003E\\u003C\\/a\\u003E\",\"text\":\"\"},\"name\":\"Wumo 02. Aug 2020\",\"post-type\":\"article\",\"_id\":\"2096\",\"_is_read\":true,\"_source\":\"4\",\"_channel\":{\"name\":\"Comics\",\"id\":\"3\"}},{\"type\":\"entry\",\"author\":{\"name\":wumo,\"url\":\"https:\\/\\/xkcd.com\\/\",\"photo\":\"null\"},\"uid\":\"https:\\/\\/xkcd.com\\/2340\\/\",\"url\":\"https:\\/\\/xkcd.com\\/2340\\/\",\"published\":\"2020-07-31T04:00:00+00:00\",\"content\":{\"html\":\"\\u003Cimg src=\\u0022https:\\/\\/imgs.xkcd.com\\/comics\\/cosmologist_genres.png\\u0022 title=\\u0022Inflationary cosmologists call all music from after the first 10^-30 seconds \\u0026quot;post-\\u0026quot;\\u0022 alt=\\u0022cosmologist_genres.png\\u0022 \\/\\u003E\\u003Cbr \\/\\u003EInflationary cosmologists call all music from after the first 10^-30 seconds \\u0022post-\\u0022\",\"text\":\"Inflationary cosmologists call all music from after the first 10^-30 seconds \\u0022post-\\u0022\"},\"name\":\"Cosmologist Genres\",\"post-type\":\"article\",\"_id\":\"2097\",\"_is_read\":true,\"_source\":\"5\",\"_channel\":{\"name\":\"Comics\",\"id\":\"3\"}}]}";
        List<TimelineItem> items = r.parseTimelineResponse(timelineResponse, "3", "Comics", entries, false, false, false, olderItems);
        assertEquals("Wumo 02. Aug 2020", items.get(0).getName());
    }

}

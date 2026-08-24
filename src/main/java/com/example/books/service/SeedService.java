package com.example.books.service;

import com.example.books.api.dto.AuthorDto;
import com.example.books.api.dto.CreateAuthorRequest;
import com.example.books.api.dto.TagDto;
import com.example.books.api.dto.CreateBookRequest;
import com.example.books.repo.AuthorRepository;
import com.example.books.repo.BookRepository;
import com.example.books.repo.TagRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedService {

  private static final String[] TAGS = {
      "רומן",
      "סיפור קצר",
      "שירה",
      "היסטוריה",
      "פילוסופיה",
      "מדע",
      "אמנות",
      "ביוגרפיה",
      "פסיכולוגיה",
      "כלכלה",
      "מדע בדיוני",
      "פנטזיה",
      "מתח",
      "פשע",
      "ילדים",
      "נוער",
      "בישול",
      "בריאות",
      "מסעות",
      "דת"
  };

  private static final String[] AUTHORS = {
      "עמוס עוז",
      "א.ב. יהושע",
      "דויד גרוסמן",
      "מאיר שלו",
      "אורלי קסטל-בלום",
      "אתגר קרת",
      "סייד קשוע",
      "דורית רביניאן",
      "יהושע קנז",
      "שלי ילין-פלג",
      "נווה יניר",
      "רון אילון",
      "אילנה ברנשטיין",
      "חיים באר",
      "יהודה אטלס",
      "יעל דיין",
      "עדי אלטשולר",
      "מיכל זמיר",
      "חנה אברם",
      "אורן נווה"
  };

  private static final String[] PUBLISHERS = {
      "עם עובד", "כתר", "ידיעות אחרונות", "מודן", "כנרת",
      "הוצאת ספרית פועלים", "ספרי עליית הגג", "הוצאת ספרים חדשה",
      "תל אביב ספרים", "דני ספרים"
  };

  private static final String[] TITLES = {
      "סיפור על אהבה וחושך",
      "ימים של צדק",
      "מישהו לרוץ איתו",
      "אדוני ראש הממשלה",
      "דעתו של הקיום",
      "מפלגת הבוהמה הגדולה",
      "קרן אור",
      "דרך הקיסר",
      "תיקון חצות",
      "החיים כמשל",
      "בניה ראשונה",
      "לא אוהב את הים",
      "בין שני עולמות",
      "האש על הגבעה",
      "ילדה עם עגבניות",
      "הרי האפר",
      "פנים ושמות",
      "מקום אחר",
      "מסע אל קצה הלילה",
      "שחר הקיץ",
      "בית הבד",
      "נחמה",
      "שירת המכונית",
      "עיר ללא אלוהים",
      "המלך הולך ומתקרב",
      "ימי זעם",
      "אנשים טובים",
      "חולמים על פחד",
      "מעבר לים",
      "בצל הקשת",
      "האיש שהיה שישי",
      "מעגל החיים",
      "סיפורי אבות",
      "מבעד לזכוכית",
      "האורות האחרונים",
      "שערי תשובה",
      "מדרשות האור",
      "בין הכרמים",
      "הרוח והקנאה",
      "נשים בזמן",
      "על קצה הקרחון",
      "לילה בקיסריה",
      "ים של דמעות",
      "השמיים של פלרי",
      "מרחק נגיעה",
      "שבילי החול",
      "אחים לנשק",
      "בלדה למכונית",
      "רחוב הנביאים",
      "תחנות בדרך",
      "מרפסת לים",
      "אנתולוגיה של שקט",
      "דברים שאני יודע",
      "שמורת הטבע",
      "מעבר להרים",
      "הרוח הצהובה",
      "בית קפה בירושלים",
      "השעה האפורה",
      "אני רוצה לגדול",
      "נערה עם כיפה",
      "מסע בין כוכבים",
      "סודות המלך",
      "השבועה",
      "בין ערים",
      "נהר של זהב",
      "מסתורין בארמון",
      "חלון לים",
      "גן נעול",
      "רעש לבן",
      "שירת הים",
      "האיש בצל",
      "ממלכת החיות",
      "על גג העולם",
      "דרך ארוכה לבית",
      "לילות קיץ",
      "משחקי ילדים",
      "השקט שלפני",
      "כשהגשם יורד",
      "אש בלילה",
      "מעבר לחומות",
      "האחרון בקהילה",
      "צלילים שלא נשמעים",
      "בקצה הדרך",
      "ימי חסד",
      "אישה בורחת",
      "סיפורים מהמרפסת",
      "דמעות של מלאכים",
      "ממלכת הצללים",
      "חלומות של אחרים",
      "המפתח האבוד",
      "עלייה לרגל",
      "בין הדפים",
      "רוחות של שינוי",
      "עיר של חול",
      "האיש שידע יותר מדי",
      "שביל החלב",
      "מעגל השמש",
      "בית של אבנים",
      "כוכבים בחושך",
      "יער של סודות",
      "נשיקה באפלה",
      "המסע ללא מילים",
      "מראות וצלילים",
      "אשת הברזל",
      "ממדים אחרים",
      "השעון המכונף",
      "מסע אל העבר",
      "נופים של זיכרון",
      "רוחות מהים",
      "לב של אבן",
      "שירת העננים",
      "מעל הגלים",
      "אורות בערפל",
      "המסתורין הנעלם",
      "בין הרים ועמקים",
      "דרך החלב והדבש",
      "המלחמה שלא נגמרה",
      "סיפורי אהבה",
      "חיים של אחרים",
      "שערי תקווה",
      "מכתבים לאמו",
      "רקוויאם לחלום",
      "מסע בין העולמות",
      "זרעים של אור",
      "ילדה בוערת",
      "קולות מהעבר",
      "רוח צפונית",
      "מסתורין במוזיאון",
      "ימים של אור",
      "לילה ללא ירח",
      "מבצר בקצה העולם",
      "שבילים נסתרים",
      "המשפחה המופלאה",
      "רוחות של קיץ",
      "ים של זיכרונות",
      "מעבר לקשת",
      "בית קטן ביער",
      "חלונות לגן עדן",
      "מכתב אהבה",
      "מסע אל הלב",
      "אישה עם כנפיים",
      "שירת הזמן",
      "נופי הנשמה",
      "אור בקצה המנהרה",
      "מבצר של סודות",
      "ימים של שלכת",
      "רוחות של אהבה",
      "מסע בין הכוכבים",
      "בית של סיפורים",
      "שירת הדרכים"
  };

  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;
  private final TagRepository tagRepository;
  private final TagService tagService;
  private final AuthorService authorService;
  private final BookService bookService;

  private final Random random = new Random();

  public SeedService(
      BookRepository bookRepository,
      AuthorRepository authorRepository,
      TagRepository tagRepository,
      TagService tagService,
      AuthorService authorService,
      BookService bookService) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
    this.tagRepository = tagRepository;
    this.tagService = tagService;
    this.authorService = authorService;
    this.bookService = bookService;
  }

  @Transactional
  public Result seed() {
    bookRepository.deleteAll();
    bookRepository.flush();
    authorRepository.deleteAll();
    authorRepository.flush();
    tagRepository.deleteAll();
    tagRepository.flush();

    List<TagDto> tags = new ArrayList<>();
    for (String name : TAGS) {
      tags.add(tagService.create(name));
    }

    List<AuthorDto> authors = new ArrayList<>();
    for (String fullName : AUTHORS) {
      CreateAuthorRequest req = new CreateAuthorRequest();
      String[] parts = fullName.split(" ", 2);
      req.setFirstName(parts[0]);
      req.setLastName(parts.length > 1 ? parts[1] : "");
      authors.add(authorService.create(req));
    }

    int created = 0;
    for (int i = 0; i < TITLES.length; i++) {
      CreateBookRequest req = new CreateBookRequest();
      req.setTitle(TITLES[i]);
      req.setPublisher(randomPublisher());
      req.setYear(1950 + random.nextInt(75));
      req.setIsbn(randomIsbn());
      req.setWishlist(random.nextDouble() < 0.2);
      req.setAuthorIds(randomSubset(authors, 1, 3, AuthorDto::id));
      req.setTagIds(randomSubset(tags, 1, 3, TagDto::id));
      bookService.create(req);
      created++;
    }

    return new Result(tags.size(), authors.size(), created);
  }

  private String randomPublisher() {
    return PUBLISHERS[random.nextInt(PUBLISHERS.length)];
  }

  private String randomIsbn() {
    StringBuilder sb = new StringBuilder("978");
    for (int i = 0; i < 10; i++) {
      sb.append(random.nextInt(10));
    }
    return sb.toString();
  }

  private <T> List<Long> randomSubset(List<T> items, int min, int max, Function<T, Long> idExtractor) {
    int count = min + random.nextInt(max - min + 1);
    List<T> copy = new ArrayList<>(items);
    Collections.shuffle(copy, random);
    return copy.stream().limit(count).map(idExtractor).toList();
  }

  public record Result(int tags, int authors, int books) {}
}
